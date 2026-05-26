const mongoose = require('mongoose');

const userWalletSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true
    },
    coins: { type: Number, default: 0 },
    unlockedPlantIds: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Plant' }]
}, { timestamps: true });

module.exports = mongoose.model('UserWallet', userWalletSchema);