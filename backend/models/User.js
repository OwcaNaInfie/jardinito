const mongoose = require('mongoose');

const avatarSchema = new mongoose.Schema({
  default: {
    type: String,
    required: true
  },
  custom: {
    type: String,
    default: null
  },
  google: {
    type: String,
    default: null
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
