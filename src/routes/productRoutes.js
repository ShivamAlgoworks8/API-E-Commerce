const express = require("express");

const {
  createProduct,
  getProducts,
  getProductById,
  updateProduct,
  deleteProduct,
  updateProductStatus,
} = require("../controllers/productController");

const router = express.Router();

router.post("/", createProduct);

router.get("/", getProducts);

router.get("/:id", getProductById);

router.put("/:id", updateProduct);

router.delete("/:id", deleteProduct);

router.patch("/:id/status", updateProductStatus);

module.exports = router;