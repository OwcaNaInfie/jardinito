const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const cors = require('cors');
const { startCronJobs } = require('./utils/cronService');

// Load .env
dotenv.config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

app.use('/avatars', express.static('public/avatars'));
app.use('/plants', express.static('public/plants'));

// Log every incoming request
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} - body:`, req.body);
    next();
});

// Routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/user');
const tagRoutes = require('./routes/tags');
const plantRoutes = require('./routes/plants');
const sessionRoutes = require('./routes/sessions');
const walletRoutes = require('./routes/wallet');

app.use('/api/auth', authRoutes);
app.use('/api/user', userRoutes);
app.use('/api/tags', tagRoutes);
app.use('/api/plants', plantRoutes);
app.use('/api/sessions', sessionRoutes);
app.use('/api/wallet', walletRoutes);

// Connect to MongoDB
mongoose.connect(process.env.MONGO_URI)
  .then(() => {
  console.log('MongoDB connected');
  startCronJobs();
  })
  .catch(err => console.log(err));

// Root route
app.get("/", (req, res) => {
  res.send("Jardinito backend is running 🌱");
});

// Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
