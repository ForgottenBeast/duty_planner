package duty_planner::View::Web;
use Moose;
use namespace::autoclean;

extends 'Catalyst::View::TT';

__PACKAGE__->config(
    TEMPLATE_EXTENSION => '.tt',
    render_die => 1,
);

=head1 NAME

duty_planner::View::Web - TT View for duty_planner

=head1 DESCRIPTION

TT View for duty_planner.

=head1 SEE ALSO

L<duty_planner>

=head1 AUTHOR

root

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;
