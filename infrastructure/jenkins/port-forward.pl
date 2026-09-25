#!/usr/bin/perl
use strict;
use warnings;
use IO::Socket::INET;
use IO::Select;

my $local_port = $ARGV[0] or die "Usage: $0 <local_port> <remote_host> <remote_port>\n";
my $remote_host = $ARGV[1] or die "Usage: $0 <local_port> <remote_host> <remote_port>\n";
my $remote_port = $ARGV[2] or die "Usage: $0 <local_port> <remote_host> <remote_port>\n";

my $server = IO::Socket::INET->new(
    LocalPort => $local_port,
    Type      => SOCK_STREAM,
    Reuse     => 1,
    Listen    => 10
) or die "Cannot bind to local port $local_port: $!\n";

my $select = IO::Select->new($server);
my %peers;

while (my @ready = $select->can_read) {
    for my $fh (@ready) {
        if ($fh == $server) {
            my $client = $server->accept();
            next unless $client;

            # Connect to target service with retry to accommodate service startup/delays
            my $target;
            for (my $attempt = 0; $attempt < 30; $attempt++) {
                $target = IO::Socket::INET->new(
                    PeerAddr => $remote_host,
                    PeerPort => $remote_port,
                    Proto    => 'tcp',
                    Timeout  => 2
                );
                last if $target;
                select(undef, undef, undef, 0.5); # sleep 500ms
            }

            if (!$target) {
                close($client);
                next;
            }

            $select->add($client);
            $select->add($target);
            $peers{$client} = $target;
            $peers{$target} = $client;
        } else {
            my $peer = $peers{$fh};
            my $buffer;
            my $bytes = sysread($fh, $buffer, 8192);
            if (!defined $bytes || $bytes == 0) {
                $select->remove($fh);
                $select->remove($peer) if $peer;
                delete $peers{$peer} if $peer;
                delete $peers{$fh};
                close($fh);
                close($peer) if $peer;
            } else {
                syswrite($peer, $buffer) if $peer;
            }
        }
    }
}
