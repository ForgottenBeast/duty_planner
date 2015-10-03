services= new Mongo.Collection('services');
medecins = new Mongo.Collection('medecins');
feries = new Mongo.Collection('feries');
info = new Mongo.Collection('info');
options = new Mongo.Collection('options');
vacances = new Mongo.Collection('vacances');
UserSession = new Meteor.Collection("user_sessions");

if (Meteor.isClient) {

  Meteor.startup(function () {
  	id = Random.id();
	Session.set('id',id);
	console.log("adduser calling");
	Meteor.call('adduser',id);
  });
	Template.serviceform.helpers({
	'data': function(){
		if(services != null){
			return services.find({id: id}).fetch();
		}
	},
	'selectedClass': function(){
		var serviceid = this._id;
		var service = Session.get('service');
		if(serviceid == service){
			return "selected";
		}
	},

	'service': function(){
		return services.find({id: id});
	}


  })

  	Template.medform.helpers({
		'data': function(){
			if(medecins != null){
				return medecins.find({id: id}).fetch();
			}
		},
		'service_data':function(){
			if(services != null){
				return services.find({id: id}).fetch();
			}
		},
		'selectedmed': function(){
			var medid = this._id;
			var med = Session.get('medecin');
			if(medid == med){
				return "selectedmed";
			}
		},

		'medecins': function(){
			return medecins.find({id: id});
		}


	})
	Template.ferieform.helpers({
		'med_data': function(){
			if(medecins != null){
				return medecins.find({id: id}).fetch();
			}
		},
		'data': function(){
			if(feries != null){
				return feries.find({id: id}).fetch();
			}
		},
		'selferie': function(){
			var medid = this._id;
			var med = Session.get('ferie');
			if(medid == med){
				return "selferie";
			}
		},

		'feries': function(){
			return feries.find({id: id});
		}


	})
	Template.infoform.helpers({
		'data': function(){
			if(info != null){
				return info.find({id: id}).fetch();
			}
		},
		'seldates': function(){
			var medid = this._id;
			var med = Session.get('date');
			if(medid == med){
				return "seldates";
			}
		},

		'info': function(){
			return info.find({id: id});
		}


	})
	Template.optionform.helpers({
		'data': function(){
			if(options != null){
				return options.find({id: id}).fetch();
			}
		},
		'med_data': function(){
			if(medecins != null){
				return medecins.find({id: id}).fetch();
			}
		},
		'seloption': function(){
			var medid = this._id;
			var med = Session.get('option');
			if(medid == med){
				return "seloption";
			}
		},

		'options': function(){
			return options.find({id: id});
		}


	})
	Template.holidaysform.helpers({
		'getid': function(){
			return id;
		},
		'med_data': function(){
			if(medecins != null){
				return medecins.find({id: id}).fetch();
			}
		},
		'data': function(){
			if(vacances != null){
				return vacances.find({id: id}).fetch();
			}
		},
		'selvacances': function(){
			var medid = this._id;
			var med = Session.get('vacances');
			if(medid == med){
				return "selvacances";
			}
		},

		'options': function(){
			return vacances.find({id: id});
		},

	})
	Template.fupload.helpers({
		'getid': function(){
			return UserSession.find({user: id}).fetch()[0].server;
			}
	})

  Template.serviceform.events({
	'submit form': function(event){
		event.preventDefault();
		var nom_service= event.target.nom_service.value;
		var nb_jours_repos = event.target.nb_jours_repos.value;
		var interieur = event.target.interieur.checked;
		console.log("interieur = "+interieur);
		console.log(event.type);
		if(services.find({nom: nom_service, id: id}).count() == 0){
			Meteor.call('update_services',nom_service,nb_jours_repos,interieur,id);
		}
	},

	
	'click .service': function(){
		var serviceid = this._id;
		Session.set('service',serviceid);
		var leservice = Session.get('service');
	},

	'click .remove': function(){
		var selservice = Session.get('service');
		Meteor.call('remove_service',selservice);
	}
  });

  Template.medform.events({
	  'submit form': function(event){
			event.preventDefault();
			var nom_med= event.target.nom_med.value;
			var service = event.target.service.value;
			var dernieregarde= event.target.dernieregarde.value;
			console.log(event.type);
			if(services.find({nom: service, id: id}).count()==1 && medecins.find({nom: nom_med, id: id}).count() == 0){
				Meteor.call('update_med',nom_med,service,dernieregarde,id);
			}
		},

		
		'click .medecin': function(){
			var medid = this._id;
			Session.set('medecin',medid);
		},

		'click .remove': function(){
			var selmed = Session.get('medecin');
			Meteor.call('remove_med',selmed);
		}
  });
  Template.ferieform.events({
	  'submit form': function(event){
			event.preventDefault();
			var nom_med= event.target.nom_med.value;
			var ladate = new Date(event.target.date_ferie.value);
			var interieur = event.target.interieur.checked;
			console.log(event.type);
			if(medecins.find({nom: nom_med, id: id}).count() == 1){
				Meteor.call('update_ferie',nom_med,ladate,interieur,id);
			}
		},

		
		'click .ferie': function(){
			var leferie = this._id;
			Session.set('ferie',leferie);
		},

		'click .remove': function(){
			var selferie = Session.get('ferie');
			Meteor.call('remove_ferie',selferie);
		}
  });

 Template.infoform.events({
	  'submit form': function(event){
			event.preventDefault();
			var datedebut= new Date(event.target.datedebut.value);
			var datefin = new Date( event.target.datefin.value);
			
			var tmp = new Date();
			if(datedebut > datefin){
				tmp = datedebut;
				datedebut = datefin;
				datefin = tmp;
			}
			console.log(event.type);
			if(info.find({id: id}).count() == 0){
				Meteor.call('update_info',datedebut,datefin,id);
			}
		},

		
		'click .infodate': function(){
			var leferie = this._id;
			Session.set('date',leferie);
		},

		'click .remove': function(){
			var selferie = Session.get('date');
			Meteor.call('remove_info',selferie);
		}
  });
Template.optionform.events({
	  'submit form': function(event){
			event.preventDefault();
			var nom= event.target.lenom.value;
			var nblundi= event.target.nblundi.value;
			var nbmardi= event.target.nbmardi.value;
			var nbmercredi= event.target.nbmercredi.value;
			var nbjeudi= event.target.nbjeudi.value;
			var nbvendredi= event.target.nbvendredi.value;
			var nbsamedi= event.target.nbsamedi.value;
			var nbdimanche= event.target.nbdimanche.value;
			var nbtotal= event.target.nbtotal.value;
			console.log(event.type);
			Meteor.call('update_options',nom,nblundi,nbmardi,nbmercredi,nbjeudi,nbvendredi,nbsamedi,nbdimanche,nbtotal,id);
		},
		
		'click .infoption': function(){
			var leferie = this._id;
			Session.set('option',leferie);
		},

		'click .remove': function(){
			var selferie = Session.get('option');
			Meteor.call('remove_option',selferie);
		},
  });

Template.holidaysform.events({
	  'submit form': function(event){
			event.preventDefault();
			var datedebut= new Date(event.target.datedebut.value);
			var datefin = new Date(event.target.datefin.value);
			var tmp = new Date();
			var nom = event.target.nom.value;
			console.log(event.type);
			if(datedebut > datefin){
				tmp = datedebut;
				datedebut = datefin;
				datefin = tmp;
			}
			if(medecins.find({nom: nom,id: id}).count() == 1){
				Meteor.call('update_vacances',datedebut,datefin,nom,id);
			}
		},

		
		'click .vacances': function(){
			var leferie = this._id;
			Session.set('vacances',leferie);
		},

		'click .remove': function(){
			var selferie = Session.get('vacances');
			Meteor.call('remove_vacances',selferie);
		},
		'click .generer': function(){
			Meteor.call('write_csv',id);
		}
  });


}

	Router.map(function(){
		this.route('home', {path: '/'});
		this.route('serviceform');
		this.route('infoform');
		this.route('holidaysform');
		this.route('optionform');
		this.route('medform');
		this.route('ferieform');
		this.route('fupload');
	});

if (Meteor.isServer) {
Meteor.startup(function () {
	myid = Random.id();
  	UploadServer.init({
		tmpDir: process.env.PWD+'/appfiles/tmp',
		uploadDir: process.env.PWD+'/appfiles/',
		checkCreateDirectories: true,
		getFileName: function(fileInfo,formData){
			var id= myid;
			console.log("returning "+id+"_data.xls");
			return id+"_data.xls";
		},
		finished: function(fileInfo,formFields){
			var id = myid;
			console.log("finished id = "+id+" myid = "+myid);
			var exec = Meteor.npmRequire('child_process').exec,child;
			child= exec("cd "+process.env.PWD+"/appfiles; java -jar duty_planner.jar --xls "+id+"_data.xls "+id+"; zip "+id+"fichiers.zip "+id+"_data.xls "+id+"_planning_garde.xls; rm "+id+" "+id+"_data.xls "+id+"_planning_garde.xls; echo '+1' >> "+process.env.PWD+"/appfiles/totalusers;");
			Meteor.call('remove_session',id);
			Meteor._sleepForMs(5000);
		}
	})

   });

  	    function to_java_date(ladate){
  // GET CURRENT DATE
  var date = new Date(ladate);
   
   // GET YYYY, MM AND DD FROM THE DATE OBJECT
   var yyyy = date.getFullYear().toString();
   var mm = (date.getMonth()+1).toString();
   var dd  = date.getDate().toString();
    
    // CONVERT mm AND dd INTO chars
    var mmChars = mm.split('');
    var ddChars = dd.split('');
     
     // CONCAT THE STRINGS IN YYYY-MM-DD FORMAT
     var datestring = yyyy + '-' + (mmChars[1]?mm:"0"+mmChars[0]) + '-' + (ddChars[1]?dd:"0"+ddChars[0]);
     return datestring;
     }
var fs = Npm.require('fs');

var fail = function(response) {
  response.statusCode = 404;
    response.end();
    };
var datafile = function(){
		id=this.params.id;
		var file=process.env.PWD+"/appfiles/"+id+"fichiers.zip";
		console.log("trying to download "+file);
		var stat = null;
		 try {
		     stat = fs.statSync(file);
		       } catch (_error) {
		           return fail(this.response);
		     }
		var attachmentFilename="planning_gardes.zip";
		stat = fs.statSync(file);
		  this.response.writeHead(200, {
		      'Content-Type': 'application/zip',
		      'Content-Disposition': 'attachment; filename='+attachmentFilename,
		      'Content-Length': stat.size
		});
		       fs.createReadStream(file).pipe(this.response);
		var exec = Meteor.npmRequire('child_process').exec,child;
		child=exec("sleep 60; rm "+file);
		UserSession.remove({server: id});

	};
Router.route('data/:id',datafile,{where: 'server'});
    Meteor.methods({
    	'adduser': function(userid){
			myid=Random.id();
			var thisid = myid;
			UserSession.insert({server: thisid, user: userid});
			console.log("added user id "+userid+" linked to serverid "+thisid);
setTimeout(function(){Meteor.call('remove_session',userid);console.log("account "+userid+"deleted");},86400000); //delete session account every 24 hours
	},
	
	'update_services':function(nom_service,nb_jours_repos,interieur,id){
		services.insert({nom: nom_service, repos: nb_jours_repos,interieur: interieur, id: id});
		console.log("updated services, id = "+id);
	},
	'update_med':function(nom_med,service,dernieregarde,id){
			medecins.insert({nom: nom_med, service: service,lastshift: dernieregarde,id : id});
		console.log("updated med, id = "+id);
	},
	'update_ferie':function(nom_med,ladate,interieur,id){
			feries.insert({nom: nom_med, date: ladate,interieur: interieur,id : id});
		console.log("updated ferie id = "+id);
	},

	'update_info':function(datedebut,datefin,id){
			info.insert({datedebut: datedebut, datefin: datefin,id: id});
		console.log("updated info id = "+id);
	},

	'update_options':function(nom,nblundi,nbmardi,nbmercredi,nbjeudi,nbvendredi,nbsamedi,nbdimanche,nbtotal,id){
			options.insert({ nom: nom, nblundi: nblundi, nbmardi: nbmardi, nbmercredi: nbmercredi,
			nbjeudi: nbjeudi, nbvendredi: nbvendredi, nbsamedi: nbsamedi, nbdimanche: nbdimanche,
			nbtotal: nbtotal,id: id});
		console.log("updated options,id = "+id);
	},
	'update_vacances':function(datedebut,datefin,nom,id){
			vacances.insert({datedebut: datedebut, datefin: datefin, nom: nom,id: id});
			console.log("updated vacances,id : "+id);
	},
	'remove_vacances':function(selferie){
		vacances.remove(selferie);
	},

	'remove_service':function(selservice){
		services.remove(selservice);
  	},
	'remove_med':function(selmed){
			medecins.remove(selmed);
	},
	'remove_ferie':function(selferie){
			feries.remove(selferie);
	},
	'remove_info':function(selferie){
			info.remove(selferie);
	},
	'remove_option':function(selferie){
			options.remove(selferie);
	},
	'remove_session': function(id){
		UserSession.remove({user: id});
		console.log("removed session userid: "+id);
	},

	'write_csv':function(id){
		var myPath=process.env.PWD+'/appfiles'
		var filepath = myPath+"/"+id
		var file = fs.createWriteStream(filepath);
		file.on('error',function(err){console.log(err.reason)});
		file.write("<medecins>\n");
		marray = medecins.find({id: id}).fetch();
		marray.forEach(function(v){file.write(v.nom+'\n'+v.service+'\n'+v.lastshift+'\n')});
		file.write("</medecins>\n<feries>\n");
		marray = feries.find({id: id}).fetch();
		marray.forEach(function(v){file.write(to_java_date(v.date)+'\n'+v.nom+'\n'+v.interieur+'\n')});
		file.write("</feries>\n<vacances>\n");
		marray = vacances.find({id: id}).fetch();
		marray.forEach(function(v){file.write(to_java_date(v.datedebut)+'\n'+to_java_date(v.datefin)+'\n'+v.nom+'\n')});
		file.write("</vacances>\n<info>\n");
		marray = info.find({id: id}).fetch();
		marray.forEach(function(v){file.write(to_java_date(v.datedebut)+'\n'+to_java_date(v.datefin)+'\n')});
		file.write("</info>\n<services>\n");
		marray = services.find({id: id}).fetch();
		marray.forEach(function(v){file.write(v.nom+'\n'+v.interieur+'\n'+v.repos+'\n'); });
		file.write("</services>\n<options>\n");
		marray = options.find({id: id}).fetch();
		marray.forEach(function(v){file.write(v.nom+'\n'+v.nbtotal+'\n'+v.nblundi+'\n'+v.nbmardi+'\n'+v.nbmercredi+'\n'+v.nbjeudi+'\n'+v.nbvendredi+'\n'+v.nbsamedi+'\n'+v.nbdimanche+'\n')});
		file.write("</options>");
		file.end();

		//empty database
		services.remove({id : id});
		medecins.remove({id: id});
		feries.remove({id: id});
		info.remove({id: id});
		options.remove({id: id});
		vacances.remove({id: id});
		//execute duty_planner.jar
		var exec = Meteor.npmRequire('child_process').exec,child;
		child= exec("cd "+process.env.PWD+"/appfiles; java -jar duty_planner.jar "+id+"; zip "+id+"fichiers.zip "+id+"_data.xls "+id+"_planning_garde.xls;rm "+id+" "+id+"_data.xls "+id+"_planning_garde.xls;echo '+1' >>"+process.env.PWD+"/appfiles/totalusers;");
		Meteor.call('remove_session',id);
		Meteor._sleepForMs(5000);
			
	}
  });
   
 }
