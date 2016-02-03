define(function(require) {

  'use strict';
  var React = require('react');
  var ReactDOM = require('react-dom');

  
  var LiveLog = React.createClass({
	  openWS : function(liveLogInstance){
		  if("WebSocket" in window){
			  var ws = new WebSocket(this.props.liveLogURL);
			  ws.onopen = function() {
				  // Web Socket is connected, send data using send()
	              ws.send("Coucou gamin !");
	              console.log("Welcome message is sent...");
               };
				
               ws.onmessage = function (evt) 
               { 
                  var received_msg = evt.data;
                  console.log("Message is received...");
                  liveLogInstance.append(received_msg);
               };
				
               ws.onclose = function()
               { 
                  // websocket is closed.
            	   console.log("Connection is closed...");
            	   liveLogInstance.append("No connection is closed...");
               };
		  } else {
			  liveLogInstance.append("Websocket not supported");
		  }
	  },
	  getInitialState: function() {
		  	this.openWS(this);
	  		return {messages:Array()};
	  	},
  	append: function(newMessage) {
  		var messages = this.state.messages; 
		messages.push(newMessage);	
  		this.setState({messages:messages});
  	},
  	render: function(){
		var messages=this.state.messages;
		console.log(messages);
  		return (
  			<div className="logZone">
		  		{messages.map(function(message,i){
		  			return ( <LogMessage key={"message_"+i} message={message} /> );
		  		})}
	  		</div>
  		);
  	}
  });
  
  var LogMessage = React.createClass ({
	  render:function(){
		  return(<div className="logMessage">{this.props.message}</div>);
	  }
  });
  
  LiveLog.init = function (liveLogURL) {
      ReactDOM.render(<LiveLog liveLogURL="ws://localhost:9080/logs/live"/>, document.getElementById('logApp'));
  };

  return LiveLog;

});