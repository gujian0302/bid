import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  state = {}

  constructor(props){
      super(props);
      const { socket } = this.props;
      socket.on('IMAGE', data => {
          console.log(data)
          this.setState({imgSrc: `data:image/png;base64, ${data.base64Image}`, sessionId: data.sessionId })
      })
  }

  render() {
    return (
      <div className="App">
        <div className="Input">
          <img src={this.state.imgSrc} alt="等待"  />
          <input placeholder="请输入二维码" value={this.state.input}  onChange={(e)=> this.setState({input:e.target.value})}/>
          <button onClick={() => {
              const socket = this.props.socket;
              socket.emit('CODE', {sessionId: this.state.sessionId, code: this.state.input});
              this.setState({imgSrc:'', sessionId: ''});
              console.log(this.state.input);
          }}>提交</button>
        </div>
      </div>
    );
  }
}

export default App;
