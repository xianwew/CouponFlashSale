<template>
    <div v-if="visible" 
         class="snackbar" 
         :class="{ show: visible }"
         :style="{ backgroundColor: color }">
      {{ message }}
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        visible: false,
        message: '',
        color: '#333' // Default color (dark gray)
      };
    },
    methods: {
        showMessage(msg, colorStr, duration = 3000) {
            let color;

            if (colorStr === 'red') {
            color = '#e74c3c'; // Bright Red
            } else if (colorStr === 'green') {
            color = '#2ecc71'; // Bright Green
            } else if (colorStr === 'blue') {
            color = '#3498db'; // Bright Blue
            } else if (colorStr === 'yellow') {
            color = '#f1c40f'; // Bright Yellow
            } else if (colorStr === 'orange') {
            color = '#e67e22'; // Bright Orange
            } else {
            color = '#333'; // Default Dark Gray
            }

            this.message = msg;
            this.color = color;
            this.visible = true;

            setTimeout(() => {
            this.visible = false;
            }, duration);
        }
    }
  };
  </script>
  
  <style scoped>
  .snackbar {
    position: fixed;
    bottom: 80px;
    left: 50%;
    transform: translateX(-50%);
    color: #fff;
    padding: 12px 20px;
    border-radius: 5px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    opacity: 0;
    transition: opacity 0.3s ease-in-out;
    z-index: 9999; 
    font-weight: bold;
  }
  
  .snackbar.show {
    opacity: 1;
  }
  </style>
  