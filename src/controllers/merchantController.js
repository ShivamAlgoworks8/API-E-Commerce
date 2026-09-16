const Merchant = require("../models/Merchant");

// Create Merchant
const createMerchant = async (req, res) => {
  try {
    const merchant = await Merchant.create(req.body); // Create merchant using request body

    res.status(201).json({
      success: true,
      message: "Merchant created successfully",
      data: merchant,
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message, // Send validation/error message
    });
  }
};

// Get All Merchants
const getMerchants = async (req, res) => {
  try {
    const merchants = await Merchant.find(); // Fetch all merchants from MongoDB

    res.status(200).json({
      success: true,
      count: merchants.length, // Total number of merchants
      data: merchants,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message,
    });
  }
};

// Get Single Merchant
const getMerchantById = async (req, res) => {
  try {
    const merchant = await Merchant.findById(req.params.id); // Find merchant using ID from URL

    if (!merchant) {
      return res.status(404).json({
        success: false,
        message: "Merchant not found",
      });
    }

    res.status(200).json({
      success: true,
      data: merchant,
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: "Invalid merchant ID",
    });
  }
};

// Update Merchant
const updateMerchant = async (req, res) => {
  try {
    const merchant = await Merchant.findByIdAndUpdate(
      req.params.id, // Merchant ID from URL
      req.body, // New data sent by client
      {
        new: true, // Return the updated merchant
        runValidators: true, // Apply schema validation
      }
    );

    if (!merchant) {
      return res.status(404).json({
        success: false,
        message: "Merchant not found",
      });
    }

    res.status(200).json({
      success: true,
      message: "Merchant updated successfully",
      data: merchant, // Return updated merchant
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message,
    });
  }
};

// Delete Merchant
const deleteMerchant = async (req, res) => {
  try {
    // Find merchant by ID and delete it from MongoDB
    const merchant = await Merchant.findByIdAndDelete(req.params.id);

    // If merchant doesn't exist
    if (!merchant) {
      return res.status(404).json({
        success: false,
        message: "Merchant not found",
      });
    }

    // Send success response after deletion
    res.status(200).json({
      success: true,
      message: "Merchant deleted successfully",
    });
  } catch (error) {
    // Handle invalid merchant ID
    res.status(400).json({
      success: false,
      message: "Invalid merchant ID",
    });
  }
};
// Update Merchant Status
const updateMerchantStatus = async (req, res) => {
  try {
    // Get the new status from request body
    const { status } = req.body;

    // Find merchant by ID and update only its status
    const merchant = await Merchant.findByIdAndUpdate(
      req.params.id, // Merchant ID from URL
      { status }, // New status: Available or NA
      {
        new: true, // Return the updated merchant
        runValidators: true, // Apply schema validation
      }
    );

    // If merchant doesn't exist
    if (!merchant) {
      return res.status(404).json({
        success: false,
        message: "Merchant not found",
      });
    }

    // Send updated merchant as response
    res.status(200).json({
      success: true,
      message: "Merchant status updated successfully",
      data: merchant,
    });
  } catch (error) {
    // Handle invalid ID or invalid status
    res.status(400).json({
      success: false,
      message: error.message,
    });
  }
};
module.exports = {
  createMerchant,
  getMerchants,
  getMerchantById,
  updateMerchant,
  deleteMerchant,
  updateMerchantStatus, // Export status controller for routes
};