// `main.js` is the file that sbt-web will use as an entry point
(function (requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named 'main.js"
    packages: ['godzilla'],
    paths: {
      'react': ['/webjars/react/0.14.3/react-with-addons'],
      'react-dom': ['/webjars/react/0.14.3/react-dom'],
      'JSXTransformer': ['/webjars/jsx-requirejs-plugin/0.6.0/js/JSXTransformer'],
      'jsx': ['/webjars/jsx-requirejs-plugin/0.6.0/js/jsx'],
      'jquery': ['/webjars/jquery/2.1.4/jquery'],
      'react-router' : ['/webjars/react-router/1.0.0/ReactRouter'],
      'react-router-shim': 'react-router-shim',
      'text': ['/webjars/requirejs-text/2.0.10-3/text'],
      'bootstrap': ['/webjars/bootstrap/3.3.4/js/bootstrap'],
      'react-bootstrap': ['/webjars/react-bootstrap/0.28.1/react-bootstrap']
    },
    jsx: {
      fileExtension: '.jsx'
    },
    shim : {
      'react': {
        deps: ['jquery'],
        exports: 'react'
      },
      'react-router': {
        deps:    ['react'],
        exports: 'Router'
      },
      'bootstrap': ['jquery'],
      'react-bootstrap': {
        deps: ['react', 'bootstrap'],
        exports: 'ReactBootstrap'
      }
    }
  });

  requirejs.onError = function (err) {
    console.log(err);
  };
  
  require(['jsx!app_menu'],function(AppMenu) {
	  console.log("init App Menu");
	  AppMenu.init();
  });
  
  require(['jsx!app_menu','jsx!cbe_app'], function(AppMenu, CbeApp) {
	  console.log('init Compte est bon app');
	  CbeApp.init();
  });
  
  require(['jsx!app_menu','jsx!mpl_app'], function(AppMenu, MplApp) {
	console.log('init Mot plus Long App');
	MplApp.init();
  });
  
  require(['jsx!livelog_app'],function(LiveLog){
	 console.log("init live logger App");
	 LiveLog.init();
  });

})(requirejs);
