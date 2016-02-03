define(function(require) {

  'use strict';
  var React = require('react');
  var ReactDOM = require('react-dom');
  var ReactBootstrap = require('react-bootstrap');
  var Button = ReactBootstrap.Button; 
  
  function randomConsonant() {
  	let consonnants='bcdfghjklmnpqrstvwxz';
	return consonnants[Math.round(Math.random() * (consonnants.length))]; 
  	
  }
  function randomVowel() {
	let vowels = 'aeiouy';
	return vowels[Math.round (Math.random()*(vowels.length))];
  }
  
  var MplApp = React.createClass({
  	getInitialState: function() {
  		return {};
  	},
  	render: function() {
  		return(
  		<div>
  			<MplForm submit={this.submitChallenge}/>
  			<MplSolution solutions={this.state.solutions}/>
  		</div>
  		);
  	},
  	submitChallenge: function(letters){
  		console.log("Submitting "+letters);
  		$.ajax({
  			'url':'/mpl/solve/'+letters.join(''),
  			'contentType': 'application/json',
  			'context' : this,
  			'success': function(solutions){
  				console.log(solutions);
  				this.setState({solutions: solutions});
  			}
  		});
  	}
  });
  
  var MplForm = React.createClass({
  	getInitialState : function(){
  		var initialProposal = new Array(10);
  		for(var i=0;i<10;++i){
  			if(Math.random() >0.5)
  				initialProposal[i]=randomConsonant();
  			else
  				initialProposal[i]=randomVowel();
  		}
  		return {letters:initialProposal};
  	},
  	
  	handleSubmit: function(e){
  		e.preventDefault();
  		this.props.submit(this.state.letters);
  	},
  	
  	onInputChange: function(id,e){
  		var _letters = this.state.letters;
  		_letters[id-1] = e.target.value;
  		this.setState({letters: _letters});
  	},
  	
    render: function () {
   	 var inputsArray = [1,2,3,4,5,6,7,8,9,10];
   	 var appThis=this;
      return (
      	<div>
      	<h2>Challenge</h2>
        <form className="mplForm" onSubmit={this.handleSubmit}>
			<div>
			{inputsArray.map(function(i,x) {
				return (<input size="1" maxLength="1" value={appThis.state.letters[i-1]} key={i} onChange={appThis.onInputChange.bind(null,i)}/>);
			})}
			</div>
			<Button type="submit">Find words</Button>
        </form>
        </div>
      );
    }
  });
  
  var MplSolution = React.createClass({
  	render: function() {
  		if(typeof this.props.solutions === 'undefined')
  			return (<div/>);
  		else 
	  		return(
	  			<div>
		  			<h2>Solutions</h2>
		  			<ol className="solution">
		  				{this.props.solutions.map(function(w,i) {
		  					return (<li key={i}>{w}</li>);
		  				})}
	  				</ol>
				</div>
	  		);
  	}
  });
  
  
  MplApp.init = function () {
      ReactDOM.render(<MplApp/>, document.getElementById('mplApp'));
  };

  return MplApp;

});
