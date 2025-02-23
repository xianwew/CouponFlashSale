<template>
  <div class="card d-flex flex-column flex-md-row p-3 m-2 position-relative">
    <img
      :src="coupon.imageURL"
      class="img-fluid responsive-image"
      style="object-fit: cover"
    />
    <div class="d-flex flex-column flex-grow-1 ms-3">
      <h5 class="coupon-title">{{ coupon.name }}</h5>
      <p class="text-muted">{{ coupon.description }}</p>
      <div class="mt-auto">
        <div class="price-qty d-flex justify-content-between">
          <p>
            <strong>Price:</strong> 💰{{ coupon.price }} | <strong>Qty:</strong>
            {{ coupon.quantity }}
          </p>
        </div>
        <button
          class="btn btn-primary buy-button"
          @click="buyCoupon"
          :disabled="!user || isProcessing || purchasedCoupons.includes(coupon.id)"
        >
          Buy
        </button>
      </div>
    </div>
    <Snackbar ref="snackbar" />
  </div>
</template>

<style scoped>
/* Default size for larger screens */
.responsive-image {
  width: 150px;
  height: 150px;
}

/* Full width and height for smaller screens */
@media (max-width: 767px) {
  .responsive-image {
    width: 100%;
    height: 100%;
  }

  .coupon-title {
    margin-top: 15px; /* Add margin to the title on small screens */
  }

  .price-qty {
    display: flex;
    justify-content: space-around;
    flex-direction: row;
    width: 100%;
  }

  .buy-button {
    display: block;
    width: 100%;
    margin-top: 10px;
  }
}

/* Style for larger screens */
@media (min-width: 768px) {
  .buy-button {
    position: absolute;
    bottom: 20px;
    right: 20px;
    width: 150px;
  }
}
</style>

<script>
import Snackbar from "../Snackbar/Snackbar.vue";

export default {
  components: { Snackbar },
  props: ["coupon", "user", "purchasedCoupons"],
  data() {
    return {
      stompClient: null,
      reconnectAttempts: 0,
      snackbarQueue: [],
      snackbarTimeout: null,
      isProcessing: false, // ✅ Prevents multiple purchases at once
    };
  },
  methods: {
    async buyCoupon() {
      if (!this.user) {
        this.queueSnackbar("Please login first.", "red", 3000);
        return;
      }

      this.isProcessing = true;

      try {
        const formData = new URLSearchParams();
        formData.append("userId", this.user.id);
        formData.append("couponId", this.coupon.id);

        const token = localStorage.getItem("token");
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/coupons/buy`, {
          method: "POST",
          headers: {
            Authorization: token,
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: formData.toString(),
        });

        if (!response.ok) {
          const errorMessage = await response.text();
          throw new Error(errorMessage);
        }

        const data = await response.json();
        if (data.data) {
          this.queueSnackbar("Order placed! Waiting for payment processing...", "green", 2000);
          this.initializeWebSocket(data.data);
        } else {
          throw new Error("Order ID missing from response");
        }
      } catch (error) {
        console.error("Error purchasing coupon:", error);
        this.queueSnackbar(`Error: ${error.message}`, "red", 3000);
        this.isProcessing = false;
      }
    },

    initializeWebSocket(orderId) {
      console.log("🔄 Initializing WebSocket...");

      const socket = new WebSocket(`${import.meta.env.VITE_API_BASE_WS_URL}/ws/orders/${orderId}`);

      socket.onopen = () => {
        console.log("✅ WebSocket connected!");
        this.reconnectAttempts = 0;
      };

      socket.onmessage = async (event) => {
        console.log("🔥 Message Received from WebSocket:", event.data);
        try {
          const orderStatus = JSON.parse(event.data);
          this.queueSnackbar(orderStatus.message, "blue", 2000);

          if (orderStatus.message === "Payment successful!") {
            this.$emit("update-purchased-coupons"); // ✅ Refresh purchased coupons
            this.$emit("update-user-credit"); // ✅ Refresh user balance
          } else {
            console.warn("⚠️ Payment failed for coupon:", this.coupon.id);
            this.$emit("remove-from-purchased", this.coupon.id); // ✅ Remove from purchased list
            this.isProcessing = false;
          }
        } catch (error) {
          console.warn("⚠️ Received non-JSON message, extracting status only.");
          const statusMatch = event.data.match(/update: (.*)$/);
          const statusMessage = statusMatch ? statusMatch[1] : event.data;
          console.log("✅ Extracted Status Message:", statusMessage);
          this.queueSnackbar(statusMessage, "blue", 2000);

          if (statusMessage === "Payment successful!") {
            this.$emit("update-purchased-coupons");
            this.$emit("update-user-credit");
          } else if (statusMessage.startsWith("Payment failed")) {
            console.warn("⚠️ Payment failed for coupon:", this.coupon.id);
            this.$emit("remove-from-purchased", this.coupon.id); // ✅ Remove from purchased list
            this.isProcessing = false;
          }
        }
      };

      socket.onerror = (error) => {
        console.error("⚠️ WebSocket error:", error);
        this.handleWebSocketReconnect(orderId);
      };

      socket.onclose = () => {
        console.warn("🔌 WebSocket closed. Attempting to reconnect...");
        this.handleWebSocketReconnect(orderId);
      };

      this.stompClient = socket;
    },

    handleWebSocketReconnect(orderId) {
      const maxAttempts = 5;
      const delay = Math.min(5000, Math.pow(2, this.reconnectAttempts) * 1000);

      if (this.reconnectAttempts < maxAttempts) {
        console.warn(`🔁 Retrying WebSocket connection in ${delay / 1000}s...`);
        setTimeout(() => {
          this.reconnectAttempts++;
          this.initializeWebSocket(orderId);
        }, delay);
      } else {
        console.error("🚨 Max WebSocket reconnection attempts reached.");
      }
    },

    queueSnackbar(message, color, duration) {
      this.snackbarQueue.push({ message, color, duration });
      if (!this.snackbarTimeout) {
        this.showNextSnackbar();
      }
    },

    showNextSnackbar() {
      if (this.snackbarQueue.length > 0 && this.$refs.snackbar) {
        const { message, color, duration } = this.snackbarQueue.shift();
        this.$refs.snackbar.showMessage(message, color, duration);

        this.snackbarTimeout = setTimeout(() => {
          this.snackbarTimeout = null;
          this.showNextSnackbar();
        }, duration + 500);
      }
    },
  },
  beforeUnmount() {
    if (this.stompClient) {
      this.stompClient.close();
      console.log("🔌 WebSocket disconnected.");
    }
    if (this.snackbarTimeout) {
      clearTimeout(this.snackbarTimeout);
    }
  },
};
</script>
