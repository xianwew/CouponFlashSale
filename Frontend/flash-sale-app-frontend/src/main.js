import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import 'bootstrap/dist/css/bootstrap.min.css';
import "@fortawesome/fontawesome-free/css/all.min.css";
import 'bootstrap';

// app.config.devtools = false;
window.global = window;
const app = createApp(App);
app.use(router);
app.mount('#app');