const VerificationToken = require('../models/VerificationToken');

exports.findByUserIdAndType = (userId, type) =>
    VerificationToken.findOne({ userId, type });

exports.findExpiredEmailVerification = () =>
    VerificationToken.find({
        type: 'email_verification',
        accountExpiry: { $lt: new Date() }
    });

exports.create = (data) => VerificationToken.create(data);

exports.upsert = (userId, type, data) =>
    VerificationToken.findOneAndUpdate(
        { userId, type },
        data,
        { upsert: true, new: true }
    );

exports.deleteById = (id) => VerificationToken.deleteOne({ _id: id });

exports.deleteManyByIds = (ids) => VerificationToken.deleteMany({ _id: { $in: ids } });