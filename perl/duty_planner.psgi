use strict;
use warnings;

use duty_planner;

my $app = duty_planner->apply_default_middlewares(duty_planner->psgi_app);
$app;

