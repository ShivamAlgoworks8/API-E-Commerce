const mongoose = require("mongoose");

const productSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },

  description: {
    type: String,
    required: true,
    trim: true
  },

  price: {
    type: Number,
    required: true,
    min: 0
  },

  category: {
    type: String,
    required: true,
    trim: true
  },

  stock: {
    type: Number,
    required: true,
    min: 0
  },

  status: {
    type: String,
    enum: ["Available", "NA"],
    default: "Available"
  }
});

module.exports = mongoose.model("Product", productSchema);