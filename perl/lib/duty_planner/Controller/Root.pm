=head
This file is part of duty_planner.

duty_planner is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

duty_planner is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied
warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the
GNU General Public License
along with duty_planner.  If not, see

<http://www.gnu.org/licenses/>.
=cut
package duty_planner::Controller::Root;

use Moose;
use Data::GUID;
use namespace::autoclean;
use List::MoreUtils qw(onlyidx);
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );
BEGIN { extends 'Catalyst::Controller' }

#
# Sets the actions in this controller to be registered with no prefix
# so they function identically to actions created in MyApp.pm
#
__PACKAGE__->config(namespace => '');


=encoding utf-8

=head1 NAME

duty_planner::Controller::Root - Root Controller for duty_planner

=head1 DESCRIPTION

[enter your description here]

=head1 METHODS

=head2 index

The root page (/)

=cut
my $domain = 't0rtug4.tk';
my @keys = qw(lenom nbtotal nblundi nbmardi nbmercredi nbjeudi nbvendredi nbsamedi nbdimanche);
#dont forget to set filepath at end of file : create folder
#/tmp/duty_planner_files
my $jar_folder ="/home/urist/catalyst_t0rtug4/";

sub index :Path :Args(0) {
    my ( $self, $c ) = @_;
    $c->stash(domain=>$domain);
    $c->session;
}

sub serviceform :Local :Args(0){
	my ($self,$c) = @_;
	my ($service,$jours,$interieur,$delete);
	$service = $c->request->param('nom_service');
	$jours = $c->request->param('nb_jours_repos');
	$interieur = $c->request->param('interieur');
        $delete = $c->request->param('delete');
	if($service){
            if(!$jours || $jours == 0){
                $jours = 1;
            }
            if(!$interieur){
                $interieur='false';
            }
            else{
                $interieur='true';
            }
            $c->session->{service}->{$service}={nb_jours=>$jours,interieur=>$interieur};
	}
	if($delete){
		my @todel = split(/,/,$delete);
		print "requesting deletion of @todel\n";
		for my $i (0 .. $#todel){
			delete($c->session->{service}->{$todel[$i]});
		}
	}
	$c->stash(services=>$c->session->{service});
        $c->stash(domain=>$domain);
}

sub medform :Local :Args(0){
	my ($self,$c) = @_;
        my $nom =  $c->request->param('nom_med');
        my $last_shift = $c->request->param('dernieregarde');
        my $service = $c->request->param('service');
        my $delete = $c->request->param('delete');
        if($delete){
            my @todel = split(/,/,$delete);
            for my $i (0 .. $#todel){
                delete($c->session->{medecins}->{$todel[$i]});
            }
        }
        if($nom){
            $c->session->{medecins}->{$nom}={service=>$service,dernieregarde=>$last_shift};
        }
        $c->stash(medecins=>$c->session->{medecins});
        $c->stash(services=>$c->session->{service});
        $c->stash(domain=>$domain);
}
sub ferieform :Local :Args(0){
	my ($self,$c) = @_;
        my($delete,$nom,$date,$interieur);
        $nom = $c->request->param('nom_med');
        $date = $c->request->param('date_ferie');
        $interieur=$c->request->param('interieur');
        $delete=$c->request->param('delete');
        print "delete = $delete\n";
        if(!$interieur){
            $interieur = 'false';
        }
        else{
            $interieur='true';
        }
        if($nom){
            if(!defined($c->session->{feries}->{$date}->{$interieur}) ){#no one is there
                my $service = $c->session->{medecins}->{$nom}->{service};
                my $intable = $c->session->{service}->{$service}->{interieur};
                print("intable for $nom = $intable\n");
                if($intable =~/true/ || $interieur =~ /false/ ){
                    #no contradiction between requested and capability
                    #be it as in duty our out duty
                    $c->session->{feries}->{$date}->{$interieur} = $nom;
                }
            }
        }
        if($delete){
            my @todel = split(/,/,$delete);
            for my $i (0 .. $#todel){
                my @couple = split(/_/,$todel[$i]);
                my $deldate = $couple[0];
                my $delint = $couple[1];
                delete($c->session->{feries}->{$deldate}->{$delint});
                #if I deleted the last entry then delete date
                if(($delint =~ /false/ &&
                    !defined($c->session->{feries}->{$deldate}->{true}))
                    ||($delint =~ /true/ && !defined($c->session->{feries}->{$deldate}->{false}))){
                        delete($c->session->{feries}->{$deldate});
                }
            }
        }
        $c->stash(medecins=>$c->session->{medecins});
        $c->stash(services=>$c->session->{service});
        $c->stash(ferie=>$c->session->{feries});
        $c->stash(domain=>$domain);
}
sub infoform :Local :Args(0){
	my ($self,$c) = @_;
        my $ddb = $c->request->param('datedebut');
        my $ddf = $c->request->param('datefin');
        my $delete = $c->request->param('delete');
        if($delete){
            delete($c->session->{infos});
        }
        if($ddb && $ddf && !defined($c->session->{infos})){
            $c->session->{infos} = [$ddb,$ddf];
        }
        $c->stash(infos=>$c->session->{infos});
        $c->stash(domain=>$domain);
}

sub optionform :Local :Args(0){
    my ($self,$c) = @_;
    my $delete = $c->request->param('delete');
    print "defined delete = ".defined($delete)."\n";
    if($c->request->param('lenom')){
        for my $i (1 .. $#keys){
            print("adding ".$c->request->param($keys[$i])." for $keys[$i]
                for name = ".$c->request->param($keys[0])."\n");
            $c->session->{options}->{$c->request->param($keys[0])}->[$i-1] =
                $c->request->param($keys[$i]);
        }
    }
    elsif(defined($delete)){
        my @todel = split(/,/,$delete);
        foreach my $nom (@todel){
            delete $c->session->{options}->{$nom};
        }
    }
    $c->stash(options=>$c->session->{options});
    $c->stash(medecins=>$c->session->{medecins});
    $c->stash(domain=>$domain);
}

sub holidaysform :Local :Args(0){
    my ($self,$c)=@_;
    my $nom = $c->request->param('nom');
    my $ddb = $c->request->param('datedebut');
    my $ddf= $c->request->param('datefin');
    my $delete = $c->request->param('delete');
    
    if($nom){
        $c->session->{vacances}->{$nom}->{$ddb} = $ddf;
        print "added vacances from $ddb to
        ".$c->session->{vacances}->{$nom}->{$ddb}." for $nom\n";
    }
    if($delete){
        my @todel = split(/,/,$delete);
        for my $i (0 .. $#todel){
            my @couple = split(/_/,$todel[$i]);
            my $delnom = $couple[0];
            my $deldate = $couple[1];
            delete($c->session->{vacances}->{$delnom}->{$deldate});
        }
    }
    $c->stash(domain=>$domain);
    $c->stash(vacances=>$c->session->{vacances});
    $c->stash(medecins=>$c->session->{medecins});
}

sub make_planning :Local :Args(0){
    my($self,$c) = @_;
    chdir "/tmp/duty_planner_files";
    my $filepath = $c->sessionid;
    if(! -e $filepath.'_data.xls'){
        print "no $filepath data xls\n";
        open my $csv,'>',$filepath or die "cant open dest file: $!";
        print $csv "<medecins>\n";
        my $medecins = $c->session->{medecins};
        for my $med (keys %$medecins){
            print
            $csv "$med\n$medecins->{$med}->{service}\n$medecins->{$med}->{dernieregarde}\n";
        }
        print $csv "</medecins>\n<feries>\n";
        my $feries = $c->session->{feries};
        foreach my $date (keys %$feries){
            foreach my $int (keys %{$feries->{date}}){
                print $csv "$date\n$feries->{$date}->{$int}\n$int\n";
            }
        }
        print $csv "</feries>\n<vacances>\n";
        my $vacances = $c->session->{vacances};
        foreach my $nom (keys %$vacances){
            foreach my $ddb (keys %{$vacances->{$nom}}){
                print $csv "$ddb\n$vacances->{$nom}->{$ddb}\n$nom\n";
            }
        }
        print $csv "</vacances>\n<info>\n";
        print $csv $c->session->{infos}->[0]."\n".$c->session->{infos}->[1]."\n";
        print $csv "</info>\n<services>\n";
        my $services = $c->session->{service};
        foreach my $servname (keys %$services){
            print $csv
            "$servname\n$services->{$servname}->{interieur}\n$services->{$servname}->{nb_jours}\n";
        }
        print $csv "</services>\n<options>\n";
        my $options = $c->session->{options};

        foreach my $opt (keys %$options){
            print $csv "$opt\n";
            for my $i (0 .. $#keys-1){
                print "nom = $opt\n cur key = $keys[$i]\nvalue =
                ".$options->{$opt}->[$i]."\n and i = $i\n";
                my $toprint = $options->{$opt}->[$i];
                $toprint = $i< $#keys?$toprint."\n":$toprint;
                print $csv $toprint;
            }
        }
        print $csv "</options>";
        close $csv;

        print "executing java -jar $jar_folder $filepath\n";
        my $cmd = "java -jar $jar_folder"."duty_planner.jar $filepath";
        `$cmd`;
    }
    else{
        my $wd = `pwd`;
        my $cmd = "java -jar $jar_folder"."duty_planner.jar --xls ".$filepath."_data.xls $filepath";
        print "executing $cmd from $wd\n";
        `$cmd`;
    }
    my $zip = Archive::Zip->new();
    my $file_member = $zip->addFile($filepath."_data.xls","data.xls");
    $file_member =
    $zip->addFile($c->sessionid."_planning_garde.xls","planning.xls");
       unless ( $zip->writeToFileNamed($c->sessionid.'_fichiers.zip') == AZ_OK ) {
          die 'write error';
     }

    my @files = ($c->sessionid, $c->sessionid."_data.xls",
    $c->sessionid."_planning_garde.xls");
    unlink @files;
    my $filename = $c->sessionid.'_fichiers.zip';
    open my $fh,'<',$filename;
    $c->response->header(
    'Content-Disposition' => "attachment;filename=fichiers.zip"
    );
    $c->response->body($fh);
}

sub fupload :Local :Args(0){
    my ($self,$c)=@_;
    my $filename = $c->sessionid."_data.xls";
    
    $c->stash(domain=>$domain);
    if ( $c->request->parameters->{form_submit} eq 'yes' ) {

        for my $field ( $c->req->upload ) {
            my $upload   = $c->req->upload($field);
            my $target="/tmp/duty_planner_files/$filename";
            unless ($upload->link_to($target)||$upload->copy_to($target)){
                die("Failedtocopy'$filename'to'$target':$!");
            }
        }
    }

}

=head2 default

Standard 404 error page

=cut

sub default :Path {
    my ( $self, $c ) = @_;
    $c->response->body( 'Page not found' );
    $c->response->status(404);
}

=head2 end

Attempt to render a view, if needed.

=cut

sub end : ActionClass('RenderView') {}

=head1 AUTHOR

root

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

__PACKAGE__->meta->make_immutable;

1;
