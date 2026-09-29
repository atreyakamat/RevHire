#!/bin/sh
set -eu

GATEWAY_URL="${GATEWAY_URL:-http://api-gateway:8080}"
echo "Using GATEWAY_URL: $GATEWAY_URL"

echo "Waiting for API Gateway route discovery warmup..."
WARMED=false
for i in $(seq 1 45); do
    CODE=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/test/ping" || true)
    if [ "$CODE" = "200" ]; then
        echo "API Gateway route discovery ready (HTTP 200) after ${i} attempts."
        WARMED=true
        break
    fi
    sleep 2
done

if [ "$WARMED" != "true" ]; then
    echo "ERROR: Gateway route discovery did not become ready in time."
    curl -i "$GATEWAY_URL/api/test/ping" || true
    exit 1
fi

TIMESTAMP=$(date +%s)
REG_EMAIL="testseeker_${TIMESTAMP}@test.com"
echo "=== Step 1: Register Job Seeker ($REG_EMAIL) ==="
REG_PAYLOAD=$(jq -n \
    --arg email "$REG_EMAIL" \
    --arg password "test123" \
    --arg role "JOB_SEEKER" \
    --arg firstName "Test" \
    --arg lastName "Seeker" \
    '{email: $email, password: $password, role: $role, firstName: $firstName, lastName: $lastName}')

REG_CODE=$(curl -s -o /tmp/reg.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "$REG_PAYLOAD")

if [ "$REG_CODE" != "201" ]; then
    echo "ERROR: Job seeker registration returned HTTP $REG_CODE"
    cat /tmp/reg.json || true
    exit 1
fi
echo "PASS: Job seeker registered (HTTP 201)"

echo "=== Step 2: Login Job Seeker ==="
LOGIN_PAYLOAD=$(jq -n \
    --arg email "$REG_EMAIL" \
    --arg password "test123" \
    '{email: $email, password: $password}')

LOGIN_CODE=$(curl -s -o /tmp/login.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_PAYLOAD")

if [ "$LOGIN_CODE" != "200" ]; then
    echo "ERROR: Job seeker login returned HTTP $LOGIN_CODE"
    cat /tmp/login.json || true
    exit 1
fi

LOGIN_TOKEN=$(jq -r '.token // empty' /tmp/login.json)
USER_ID=$(jq -r '.userId // empty' /tmp/login.json)

if [ -z "$LOGIN_TOKEN" ] || [ -z "$USER_ID" ]; then
    echo "ERROR: Missing token or userId in login response."
    cat /tmp/login.json
    exit 1
fi
echo "PASS: Job seeker login successful (userId=$USER_ID)"

echo "=== Step 3: Verify Profile via Gateway ==="
ME_CODE=$(curl -s -o /tmp/me.json -w "%{http_code}" -H "Authorization: Bearer $LOGIN_TOKEN" "$GATEWAY_URL/api/users/me")
if [ "$ME_CODE" != "200" ]; then
    echo "ERROR: Profile retrieval returned HTTP $ME_CODE"
    cat /tmp/me.json || true
    exit 1
fi
echo "PASS: Seeker profile verified (HTTP 200)"

echo "=== Step 4: Register Employer ==="
EMP_EMAIL="testemp_${TIMESTAMP}@test.com"
EMP_REG_PAYLOAD=$(jq -n \
    --arg email "$EMP_EMAIL" \
    --arg password "test123" \
    --arg role "EMPLOYER" \
    --arg firstName "Acme" \
    --arg lastName "Recruiter" \
    --arg companyName "Acme Corp" \
    --arg contactName "Jane Recruiter" \
    '{email: $email, password: $password, role: $role, firstName: $firstName, lastName: $lastName, companyName: $companyName, contactName: $contactName}')

EMP_CODE=$(curl -s -o /tmp/emp_reg.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "$EMP_REG_PAYLOAD")

if [ "$EMP_CODE" != "201" ]; then
    echo "ERROR: Employer registration returned HTTP $EMP_CODE"
    cat /tmp/emp_reg.json || true
    exit 1
fi

EMP_TOKEN=$(jq -r '.token // empty' /tmp/emp_reg.json)
EMP_ID=$(jq -r '.userId // empty' /tmp/emp_reg.json)
echo "PASS: Employer registered (HTTP 201, empId=$EMP_ID)"

echo "=== Step 5: Post Job as Employer ==="
JOB_PAYLOAD=$(jq -n --argjson empId "$EMP_ID" \
    '{title: "Integration Test Engineer", description: "Automated test position", location: "Remote", skills: "Java, CI/CD", salary: 95000, jobType: "FULL_TIME", employerId: $empId}')

JOB_CREATE_CODE=$(curl -s -o /tmp/job_create.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/jobs" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $EMP_TOKEN" \
    -d "$JOB_PAYLOAD")

if [ "$JOB_CREATE_CODE" != "201" ]; then
    echo "ERROR: Job creation returned HTTP $JOB_CREATE_CODE"
    cat /tmp/job_create.json || true
    exit 1
fi

TARGET_JOB_ID=$(jq -r '.id // empty' /tmp/job_create.json)
echo "PASS: Job created successfully with ID $TARGET_JOB_ID (HTTP 201)"

echo "=== Step 6: Submit Application as Seeker ==="
APP_PAYLOAD=$(jq -n --argjson jobId "$TARGET_JOB_ID" --argjson userId "$USER_ID" \
    '{jobId: $jobId, userId: $userId, resumeId: 1}')

APP_SUB_CODE=$(curl -s -o /tmp/app_sub.json -w "%{http_code}" -X POST "$GATEWAY_URL/api/applications" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $LOGIN_TOKEN" \
    -d "$APP_PAYLOAD")

if [ "$APP_SUB_CODE" != "201" ]; then
    echo "ERROR: Application submission returned HTTP $APP_SUB_CODE"
    cat /tmp/app_sub.json || true
    exit 1
fi

APP_ID=$(jq -r '.id // empty' /tmp/app_sub.json)
echo "PASS: Application submitted with ID $APP_ID (HTTP 201)"

echo "=== Step 7: Employer Updates Application Status to SHORTLISTED ==="
STATUS_CODE=$(curl -s -o /tmp/status.json -w "%{http_code}" -X PUT "$GATEWAY_URL/api/applications/$APP_ID/status?status=SHORTLISTED" \
    -H "Authorization: Bearer $EMP_TOKEN")

if [ "$STATUS_CODE" != "200" ]; then
    echo "ERROR: Application status update returned HTTP $STATUS_CODE"
    cat /tmp/status.json || true
    exit 1
fi
echo "PASS: Application status updated to SHORTLISTED (HTTP 200)"

echo "=== Step 8: Seeker Retrieves Notifications ==="
NOTIF_OK=false
for attempt in $(seq 1 15); do
    NOTIF_CODE=$(curl -s -o /tmp/notif.json -w "%{http_code}" -H "Authorization: Bearer $LOGIN_TOKEN" "$GATEWAY_URL/api/notifications/user/$USER_ID")
    if [ "$NOTIF_CODE" = "200" ] && grep -q "APPLICATION_SHORTLISTED" /tmp/notif.json 2>/dev/null; then
        echo "Notification received on attempt $attempt:"
        cat /tmp/notif.json
        NOTIF_OK=true
        break
    fi
    sleep 1
done

if [ "$NOTIF_OK" != "true" ]; then
    echo "ERROR: Timed out waiting for APPLICATION_SHORTLISTED notification."
    cat /tmp/notif.json 2>/dev/null || true
    exit 1
fi

echo "=== Clean up temporary test files ==="
rm -f /tmp/reg.json /tmp/login.json /tmp/me.json /tmp/emp_reg.json /tmp/job_create.json /tmp/app_sub.json /tmp/status.json /tmp/notif.json

echo "=== ALL MULTI-SERVICE INTEGRATION TESTS PASSED SUCCESSFULLY! ==="
