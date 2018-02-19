import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import io from 'socket.io-client';
import registerServiceWorker from './registerServiceWorker';

const socket = io('http://127.0.0.1:8080')

socket.on("connect", (data) => {
    socket.emit("CLIENT", "");
});

ReactDOM.render(<App  socket={socket}/>, document.getElementById('root'));
registerServiceWorker();
