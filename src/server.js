const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const productRoutes = require("./routes/productRoutes");
const merchantRoutes = require("./routes/merchantRoutes");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/api/products", productRoutes);
app.use("/api/merchants", merchantRoutes);

mongoose
  .connect(process.env.MONGODB_URI)
  .then(() => {
    console.log("MongoDB connected successfully");

    app.listen(process.env.PORT || 3000, () => {
      console.log(
        `Server running on http://localhost:${process.env.PORT || 3000}`
      );
    });
  })
  .catch((error) => {
    console.error("MongoDB connection failed:", error);
  });