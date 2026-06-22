const User = require('../models/User');

exports.findById = (userId) => User.findById(userId);
exports.findByEmail = (email) => User.findOne({ email });
exports.findByUsername = (username) => User.findOne({ username });
exports.findByEmailOrUsername = (identifier) =>
    User.findOne({ $or: [{ email: identifier }, { username: identifier }] });
exports.create = (userData) => User.create(userData);
exports.findByIdAndUpdate = (userId, data) =>
    User.findByIdAndUpdate(userId, data, { new: true });
exports.findByIdAndDelete = (userId) => User.findByIdAndDelete(userId);
exports.save = (user) => user.save();