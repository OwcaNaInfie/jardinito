const express = require('express');
const router = express.Router();
const UserWallet = require('../models/UserWallet');

// GET
router.get('/', async (req, res) => {
    try {
        const { userId } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const wallet = await UserWallet.findOne({ userId });
        if (!wallet) return res.status(404).json({ message: 'Wallet not found' });

        res.json({ coins: wallet.coins });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;