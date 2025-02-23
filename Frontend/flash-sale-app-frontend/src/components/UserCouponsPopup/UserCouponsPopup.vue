<template>
  <div class="modal fade" id="userCouponsModal" tabindex="-1" ref="modal" data-bs-backdrop="static">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">My Coupons</h5>
          <button type="button" class="btn-close" @click="closeModal"></button>
        </div>
        <div class="modal-body">
          <ul v-if="coupons.length">
            <li v-for="coupon in coupons" :key="coupon.id">
              {{ coupon.name }} -
              <strong> Payment {{ coupon.paymentSuccessful ? "Success" : "Failed" }}</strong>
            </li>
          </ul>
          <p v-else>No coupons purchased yet.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Modal } from "bootstrap"; // ✅ Import Bootstrap Modal

export default {
  props: ["user"],
  data() {
    return { coupons: [] };
  },
  async mounted() {
    if (this.user) {
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          console.error("User token is missing!");
          return;
        }

        const response = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/users/${this.user.id}/coupons`,
          {
            method: "GET",
            headers: {
              Authorization: token,
              "Content-Type": "application/json",
            },
          }
        );

        if (!response.ok) {
          throw new Error(`Failed to fetch coupons: ${response.status}`);
        }

        const data = await response.json();
        this.coupons = data.data || [];
        console.log(this.coupons);

        // ✅ Show the modal programmatically
        this.showModal();
      } catch (error) {
        console.error("Error fetching user coupons:", error);
      }
    }
  },
  methods: {
    showModal() {
      this.modalInstance = new Modal(this.$refs.modal);
      this.modalInstance.show();
    },
    closeModal() {
      if (this.modalInstance) {
        this.modalInstance.hide();
        this.$emit("close"); // Notify parent component to update state
      }
    },
  },
};
</script>
