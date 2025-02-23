<template>
  <div class="modal fade" id="loginModal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ isLogin ? "Login" : "Sign Up" }}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <input v-model="username" class="form-control mb-2" placeholder="Username" />
          <input
            v-if="!isLogin"
            v-model="email"
            type="email"
            class="form-control mb-2"
            placeholder="Email"
          />
          <input
            v-model="password"
            type="password"
            class="form-control mb-2"
            placeholder="Password"
          />
          <button class="btn btn-primary w-100" @click="authenticate">
            {{ isLogin ? "Login" : "Sign Up" }}
          </button>
          <p class="text-center mt-2">
            <a href="#" @click.prevent="isLogin = !isLogin">
              {{ isLogin ? "Create an account" : "Already have an account? Login" }}
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- Include Snackbar Component -->
  <Snackbar ref="snackbar" />
</template>

<script>
import Snackbar from '../Snackbar/Snackbar.vue';
import { Modal } from 'bootstrap';

export default {
    components: { Snackbar },
    emits: ['login-success'],
    data() {
        return {
            isLogin: true,
            username: '',
            email: '',
            password: ''
        };
    },
    methods: {
        async authenticate() {
            const endpoint = this.isLogin ? 'login' : 'register';
            const payload = this.isLogin
                ? { username: this.username, password: this.password }
                : { username: this.username, email: this.email, password: this.password };

            try {
                const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/users/${endpoint}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errorMessage = await response.json();
                    this.$refs.snackbar.showMessage(errorMessage.message || "Authentication failed!", "red", 3000);
                    return;
                }

                const data = response.status !== 204 ? await response.json() : null;

                if (data && data.data) {
                    this.$emit('login-success', data);
                    this.$refs.snackbar.showMessage(this.isLogin ? "Login successful!" : "Successfully signed up!", "green", 3000);

                    // ✅ Hide login modal correctly
                    const loginModalElement = document.getElementById('loginModal');
                    if (loginModalElement) {
                        const modalInstance = Modal.getInstance(loginModalElement) || new Modal(loginModalElement);
                        modalInstance.hide();
                    }
                } else {
                    this.$refs.snackbar.showMessage("Login successful, but no user data returned!", "blue", 3000);
                }
            } catch (error) {
                console.error('Auth error:', error);
                this.$refs.snackbar.showMessage("Authentication error!", "red", 3000);
            }
        }
    }
};
</script>
