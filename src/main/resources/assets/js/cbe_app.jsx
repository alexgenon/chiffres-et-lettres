define(function(require) {

  'use strict';
  var possInput = [1,2,3,4,5,6,7,8,9,10,25,50,75];
  
  function randomChallenge() {
  	var inputs = new Array(6);
  	var goal =Math.round( Math.random()*1000);
  	for(var i=0;i<6;++i)
  		inputs[i] = possInput[Math.round (Math.random()*(possInput.length))]; 
  		
  	return {goal:goal, input:inputs};
  }
  
  var React = require('react');

  var CbeApp = React.createClass({
  	getInitialState: function() {
  		return {result:{}};
  	},
  	render: function() {
  		return(
  			<div>
  			<ChallengeForm submit={this.submitChallenge}/>
  			<ChallengeSolution solution={this.state.solution}/>
  			</div>
  		);
  	},
  	submitChallenge: function(goal, input){
  		var inputStr = input.toString();
  		console.log("Submitting "+goal+" and "+inputStr);
  		$.ajax({
  			'url':'/ceb/solve/'+goal,
  			'contentType': 'application/json',
  			'data' : {'candidates': inputStr},
  			'context' : this,
  			'success': function(solution){
  				console.log(solution);
  				this.setState({solution: solution});
  			}
  		});
  	}
  });
  
  var ChallengeForm = React.createClass({
  	getInitialState : function(){
  		var _input=new Array(6);
  		var rand = randomChallenge();
  		return {goal:rand.goal, input: rand.input};
  	},
  	
  	onGoalChange: function (e){
  		this.setState({goal:e.target.value});
  	},
  	
  	handleSubmit: function(e){
  		e.preventDefault();
  		this.props.submit(this.state.goal,this.state.input);
  	},
  	
  	onInputChange: function(id,e){
  		var _inputs = this.state.input;
  		_inputs[id-1] = e.target.value;
  		this.setState({input: _inputs});
  	},
  	
    render: function () {
   	 var inputsArray = [1,2,3,4,5,6];
   	 var appThis=this;
      return (
      	<div>
      	<h2>Challenge</h2>
        <form className="cebForm" onSubmit={this.handleSubmit}>
        	<div> <label htmlFor="goal"> Goal number</label>
			<input type="number" size="3" value={this.state.goal} onChange={this.onGoalChange} /> </div>
			<div>
			{inputsArray.map(function(i,x) {
				return (<input type="number" size="3" value={appThis.state.input[i-1]} key={i} onChange={appThis.onInputChange.bind(null,i)}/>);
			})}
			</div>
			<input type="submit" value="Solve" />
        </form>
        </div>
      );
    }
  });
  
  var ChallengeSolution = React.createClass({
  	render: function() {
  		if(typeof this.props.solution === 'undefined')
  			return (<div/>);
  		else 
	  		return(
	  			<div>
		  			<h2>Solution</h2>
		  			<div className="solution">
		  				{this.props.solution.map(function(s) {
		  					return (<SolStep key={s} step={s}/>);
		  				})}
	  				</div>
				</div>
	  		);
  	}
  });
  
  var SolStep = React.createClass({
  	compute:function(step){
  		var solution=0;
  		switch(step.op){
  			case '+' : solution = step.left +step.right; break;
  			case '-' : solution = step.left -step.right; break;
  			case '*' : solution = step.left *step.right; break;
  			case '/' : solution = step.left /step.right; break;
  		}
  		return solution;
  	},
  	render:function() {
  		var s = this.props.step;
  		return(	
  			<div className="step"> {s.left+" "+s.op+" "+s.right+" = "+this.compute(s)}</div>
  		);
  	}
  });
  
  CbeApp.init = function () {
      React.render(<CbeApp/>, document.getElementById('cebApp'));
  };

  return CbeApp;

});
