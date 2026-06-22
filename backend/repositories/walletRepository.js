const UserWallet = require('../models/UserWallet');

exports.findByUserId = (userId) => UserWallet.findOne({ userId });

exports.createForUser = (userId, data) => UserWallet.create({ userId, ...data });

exports.addCoins = (userId, amount) =>
    UserWallet.findOneAndUpdate(
        { userId },
        { $inc: { coins: amount } }
    );

exports.save = (wallet) => wallet.save();