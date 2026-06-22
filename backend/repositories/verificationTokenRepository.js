const VerificationToken = require('../models/VerificationToken');

exports.findByUserIdAndType = (userId, type) =>
    VerificationToken.findOne({ userId, type });

exports.create = (data) => VerificationToken.create(data);

exports.upsert = (userId, type, data) =>
    VerificationToken.findOneAndUpdate(
        { userId, type },
        data,
        { upsert: true, new: true }
    );

exports.deleteById = (id) => VerificationToken.deleteOne({ _id: id });