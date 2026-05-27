const express = require('express');
const router = express.Router();
const UserWallet = require('../models/UserWallet');
const Plant = require('../models/Plant');

// GET wallet (coins + unlockedPlantIds)
router.get('/', async (req, res) => {
    try {
        const { userId } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const wallet = await UserWallet.findOne({ userId });
        if (!wallet) return res.status(404).json({ message: 'Wallet not found' });

        res.json({
            coins: wallet.coins,
            unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString()),
            favouritePlantIds: wallet.favouritePlantIds.map(id => id.toString())
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// POST buy plant
router.post('/buy', async (req, res) => {
    try {
        const { userId, plantId } = req.body;
        if (!userId || !plantId)
            return res.status(400).json({ message: 'userId and plantId are required' });

        const plant = await Plant.findById(plantId);
        if (!plant) return res.status(404).json({ message: 'Plant not found' });

        const wallet = await UserWallet.findOne({ userId });
        if (!wallet) return res.status(404).json({ message: 'Wallet not found' });

        if (wallet.unlockedPlantIds.map(id => id.toString()).includes(plantId))
            return res.status(400).json({ message: 'Plant already unlocked' });

        if (wallet.coins < plant.price)
            return res.status(400).json({ message: 'Insufficient coins' });

        wallet.coins -= plant.price;
        wallet.unlockedPlantIds.push(plantId);
        await wallet.save();

        res.json({
            coins: wallet.coins,
            unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString())
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// POST toggle favourite
router.post('/favourite', async (req, res) => {
    try {
        const { userId, plantId } = req.body;
        if (!userId || !plantId)
            return res.status(400).json({ message: 'userId and plantId are required' });

        const wallet = await UserWallet.findOne({ userId });
        if (!wallet) return res.status(404).json({ message: 'Wallet not found' });

        const index = wallet.favouritePlantIds.findIndex(
            id => id.toString() === plantId
        );

        if (index === -1) {
            wallet.favouritePlantIds.push(plantId);
        } else {
            wallet.favouritePlantIds.splice(index, 1);
        }

        await wallet.save();

        res.json({
            coins: wallet.coins,
            unlockedPlantIds: wallet.unlockedPlantIds.map(id => id.toString()),
            favouritePlantIds: wallet.favouritePlantIds.map(id => id.toString())
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;