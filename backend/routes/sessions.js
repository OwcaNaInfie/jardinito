const express = require('express');
const router = express.Router();
const Session = require('../models/Session');
const UserWallet = require('../models/UserWallet');

// =====================
// HELPERS
// =====================

const calculateReward = (durationMinutes) => {
    let reward = 0;
    if (durationMinutes <= 60) {
        reward = durationMinutes * 1;
    } else if (durationMinutes <= 90) {
        reward = 60 + (durationMinutes - 60) * 1.25;
    } else {
        reward = 60 + 30 * 1.25 + (durationMinutes - 90) * 1.5;
    }
    return Math.ceil(reward);
};

// =====================
// ROUTES
// =====================

// POST
router.post('/', async (req, res) => {
    try {
        const { userId, plantId, tags, plannedDuration, actualDuration, status, startedAt, completedAt } = req.body;

        if (!userId || !plantId || !plannedDuration || !status || !startedAt) {
            return res.status(400).json({ message: 'Missing required fields' });
        }

        const coinsEarned = status === 'completed' ? calculateReward(actualDuration) : 0;

        const session = await Session.create({
            userId,
            plantId,
            tags: tags || [],
            plannedDuration,
            actualDuration,
            status,
            coinsEarned,
            startedAt,
            completedAt: completedAt || null
        });

        if (status === 'completed') {
            await UserWallet.findOneAndUpdate(
                { userId },
                { $inc: { coins: coinsEarned } }
            );
        }

        res.status(201).json({ session, coinsEarned });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// GET
router.get('/', async (req, res) => {
    try {
        const { userId, from, to, status } = req.query;

        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const filter = { userId };

        if (from || to) {
            filter.startedAt = {};
            if (from) filter.startedAt.$gte = new Date(from);
            if (to) filter.startedAt.$lte = new Date(to);
        }

        if (status) filter.status = status;

        const sessions = await Session.find(filter)
            .populate('plantId')
            .sort({ startedAt: -1 });

        res.json({ sessions });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;