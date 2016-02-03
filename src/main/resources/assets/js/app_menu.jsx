define(function(require) {
	//TODO Move this code in the Twirl template : App layout ==> template
  'use strict';
  
  var React = require('react');
  var ReactDOM = require('react-dom');
  var ReactBootstrap = require('react-bootstrap');
  var Tabs = ReactBootstrap.Tabs;
  var Tab = ReactBootstrap.Tab;
  var Nav = ReactBootstrap.Nav;
  var NavItem = ReactBootstrap.NavItem;
  
  var AppMenu= React.createClass ({
	  switchApp: function (selectedKey) {
		  console.log("Switching to "+selectedKey);
		  this.setState({selectedKey:selectedKey});
	  },
	  
	  getDisabled: function(key) {
		  return (key===this.state.selectedKey);
	  },
	  
	  getInitialState: function() {
	  		return {selectedKey:1};
	  },
	  
	  render: function() {
		  return(
			<Tabs defaultActivekey={1} position="left" >
				<Tab eventKey={1} title="Le Compte est Bon">
					<div id="cebZone">
						<h2>Le compte est bon</h2>
						<div id="cebApp"></div>
					</div>
				</Tab>
				<Tab eventKey={2} title="Le Mot le Plus Long">
					<div id="mplZone">
						<h2>Le mot le plus long</h2>
						<div id="mplApp"></div>
					</div>
				</Tab>
			</Tabs>
		  );
	  }
  });
  
  
  AppMenu.init = function() {
	  ReactDOM.render(<AppMenu/>, document.getElementById('appMenu'));
  }
  
  return AppMenu;
});
