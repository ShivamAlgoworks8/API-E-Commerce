const express = require("express");

const {
  createMerchant,
  getMerchants,
  getMerchantById,
  updateMerchant,
  deleteMerchant,
  updateMerchantStatus, // Update merchant status controller
} = require("../controllers/merchantController");

const router = express.Router();

// Create merchant
router.post("/", createMerchant);

// Get all merchants
router.get("/", getMerchants);

// Get single merchant by ID
router.get("/:id", getMerchantById);

// Update merchant by ID
router.put("/:id", updateMerchant);

// Delete merchant by ID
router.delete("/:id", deleteMerchant);

// Update merchant status
router.patch("/:id/status", updateMerchantStatus);

module.exports = router;