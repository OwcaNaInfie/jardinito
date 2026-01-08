const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  username: { type: String },
  email: { type: String, required: true, unique: true },
  password: { type: String },
  provider: { type: String, required: true, default: 'local' },
  googleId: { type: String },
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
