const mongoose = require('mongoose');

const avatarSchema = new mongoose.Schema({
  type: {
    type: String,
    enum: ['default', 'google', 'custom'],
    required: true
  },
  value: {
    type: String,
    required: true
  }
}, { _id: false });

const userSchema = new mongoose.Schema({
  username: { type: String },
  email: { type: String, required: true, unique: true },
  password: { type: String },
  provider: { type: String, required: true, default: 'local' },
  googleId: { type: String },

  avatar: {
      type: avatarSchema,
      required: true
    }

}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
