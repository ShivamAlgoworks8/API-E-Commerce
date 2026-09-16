const mongoose = require("mongoose");

const merchantSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },

  email: {
    type: String,
    required: true,
    trim: true
  },

  phone: {
    type: String,
    required: true,
    trim: true
  },

  status: {
    type: String,
    enum: ["Available", "NA"],
    default: "Available"
  }
});

module.exports = mongoose.model("Merchant", merchantSchema);