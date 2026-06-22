const walletService = require('../services/walletService');

exports.getWallet = async (req, res) => {
    try {
        const { userId } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const wallet = await walletService.getWallet(userId);
        if (!wallet) return res.status(404).json({ message: 'Wallet not found' });

        res.json(wallet);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.buyPlant = async (req, res) => {
    try {
        const { userId, plantId } = req.body;
        if (!userId || !plantId)
            return res.status(400).json({ message: 'userId and plantId are required' });

        const result = await walletService.buyPlant(userId, plantId);
        res.json(result);
    } catch (err) {
        if (err.message === 'PLANT_NOT_FOUND')
            return res.status(404).json({ message: 'Plant not found' });
        if (err.message === 'WALLET_NOT_FOUND')
            return res.status(404).json({ message: 'Wallet not found' });
        if (err.message === 'PLANT_ALREADY_UNLOCKED')
            return res.status(400).json({ message: 'Plant already unlocked' });
        if (err.message === 'INSUFFICIENT_COINS')
            return res.status(400).json({ message: 'Insufficient coins' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.toggleFavourite = async (req, res) => {
    try {
        const { userId, plantId } = req.body;
        if (!userId || !plantId)
            return res.status(400).json({ message: 'userId and plantId are required' });

        const result = await walletService.toggleFavourite(userId, plantId);
        res.json(result);
    } catch (err) {
        if (err.message === 'WALLET_NOT_FOUND')
            return res.status(404).json({ message: 'Wallet not found' });
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};