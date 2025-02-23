<template>
  <div style="display: flex; flex-direction: column; min-height: 100vh; background-color: #f2f2f2;">
    <header class="d-flex justify-content-between align-items-center p-3" style="height: 100px; background-color: #dbdbf0 !important">
      <h3>Coupon Flash Sale System</h3>

      <div>
        <button v-if="!user" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#loginModal">
          Login
        </button>

        <div v-else class="d-flex align-items-center gap-3">
          <p class="mb-0 fw-bold text-success">💰 {{ user.credit }}</p>

          <button class="btn btn-outline-primary" @click="showUserCoupons">
            👤 {{ user.username }}
          </button>

          <button class="btn btn-danger" @click="logout">🚪 Logout</button>
        </div>
      </div>
    </header>

    <!-- Coupons List -->
    <div style="margin: 0px 50px;">
      <CouponCard v-for="coupon in coupons" :key="coupon.id" :coupon="coupon" :user="user"
        :purchasedCoupons="purchasedCoupons" @update-purchased-coupons="fetchPurchasedCoupons"
        @update-user-credit="fetchUserCredit" />
    </div>

    <!-- Pagination Controls -->
    <div class="d-flex justify-content-center align-items-center my-3 gap-2" style="margin-top: auto !important; margin-bottom: 30px !important; padding-top: 20px;">
      <button class="btn btn-outline-primary btn-sm d-flex align-items-center" @click="prevPage"
        :disabled="currentPage === 0">
        <i class="fa-solid fa-arrow-left"></i>
      </button>

      <p class="mb-0">Page {{ currentPage + 1 }} of {{ totalPages }}</p>

      <button class="btn btn-outline-primary btn-sm d-flex align-items-center" @click="nextPage"
        :disabled="currentPage >= totalPages - 1">
        <i class="fa-solid fa-arrow-right"></i>
      </button>
    </div>

    <!-- Footer -->
    <footer class="text-center p-3" style="margin-top: 0; height: 100px; display: flex; align-items: center; justify-content: center; background-color: #dbdbf0 !important">2025 @Lisa White<a href="https://github.com/Lizwhite8/SimpleTicketFlashSaleFullStack" style="margin-left: 20px;">Project Github</a></footer>

    <!-- Components -->
    <LoginPopup @login-success="setUser" />
    <UserCouponsPopup v-if="showCouponsPopup" :user="user" @close="showCouponsPopup = false" />
  </div>
</template>

<script>
import CouponCard from "./components/CouponCard/CouponCard.vue";
import LoginPopup from "./components/LoginPopup/LoginPopup.vue";
import UserCouponsPopup from "./components/UserCouponsPopup/UserCouponsPopup.vue";

export default {
  components: { CouponCard, LoginPopup, UserCouponsPopup },
  data() {
    return {
      user: null,
      coupons: [],
      purchasedCoupons: [],
      showCouponsPopup: false,
      token: null,
      currentPage: 0,
      totalPages: 1, // Default value to prevent issues
      pageSize: 8, // Default number of coupons per page
    };
  },

  async mounted() {
    this.restoreSession();
    this.fetchCoupons();
  },

  methods: {
    async setUser(loginResponse) {
      if (!loginResponse || !loginResponse.data || typeof loginResponse.data !== "string") {
        return;
      }

      this.token = loginResponse.data.trim();
      localStorage.setItem("token", this.token);

      try {
        const tokenParts = this.token.split(".");
        if (tokenParts.length !== 3) throw new Error("Invalid token format");

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const userId = decodedPayload.sub;

        if (!userId) throw new Error("User ID missing in token");

        localStorage.setItem("userId", userId);
        await this.verifySession(userId);
      } catch (error) {
        console.error("Error decoding JWT:", error);
        this.logout();
      }
    },

    async restoreSession() {
      const storedToken = localStorage.getItem("token");
      const storedUserId = localStorage.getItem("userId");

      if (storedToken && storedUserId) {
        this.token = storedToken;
        await this.verifySession(storedUserId);
      }
    },

    async verifySession(userId) {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/users/${userId}`, {
          headers: { Authorization: this.token },
        });

        if (!response.ok) throw new Error(`Session verification failed: ${response.status}`);

        const userData = await response.json();
        this.user = userData.data;

        await this.fetchPurchasedCoupons();
      } catch (error) {
        console.error("❌ Invalid session:", error);
        this.logout();
      }
    },

    async fetchCoupons() {
      try {
        // console.log(import.meta.env.VITE_API_BASE_URL);
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/coupons?page=${this.currentPage}&size=${this.pageSize}`);
        if (!response.ok) throw new Error(`Failed to fetch coupons: ${response.status}`);

        const jsonData = await response.json();
        // console.log(json);
        this.coupons = jsonData.data.coupons || [];
        this.totalPages = jsonData.data.totalPages || 1; // ✅ Update total pages
      } catch (error) {
        console.error("Error fetching coupons:", error);
      }
    },

    async fetchPurchasedCoupons() {
      if (!this.user) return;
      try {
        const token = localStorage.getItem("token");
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/users/${this.user.id}/coupons`, {
          headers: { Authorization: token },
        });

        if (!response.ok) throw new Error(`Failed to fetch: ${response.status}`);

        const data = await response.json();
        this.purchasedCoupons = data.data
          .filter(coupon => coupon.paymentSuccessful)
          .map(coupon => coupon.id);
      } catch (error) {
        console.error("❌ Error fetching purchased coupons:", error);
      }
    },

    async fetchUserCredit() {
      if (!this.user) return;
      try {
        setTimeout(async () => {
          console.log('fetching user credit');
          const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/users/${this.user.id}`, {
            headers: { Authorization: this.token },
          });

          if (!response.ok) throw new Error(`Failed to fetch user: ${response.status}`);

          const userData = await response.json();
          // ✅ Ensures Vue detects the change
          this.user = Object.assign({}, this.user, { credit: userData.data.credit });

          console.log("✅ User credit updated:", this.user.credit);
        }, 200);
      } catch (error) {
        console.error("❌ Error fetching user credit:", error);
      }
    },

    async logout() {
      localStorage.removeItem("token");
      localStorage.removeItem("userId");
      this.user = null;
      this.token = null;
      this.purchasedCoupons = [];
    },

    showUserCoupons() {
      this.showCouponsPopup = true;
    },

    async prevPage() {
      if (this.currentPage > 0) {
        this.currentPage--;
        await this.fetchCoupons();
      }
    },

    async nextPage() {
      if (this.currentPage < this.totalPages - 1) {
        this.currentPage++;
        await this.fetchCoupons();
      }
    },
  },
};
</script>
