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

const calculateRewardDev = (durationSeconds) => {
    let reward = 0;
    if (durationSeconds <= 5) {
        reward = durationSeconds * 1;
    } else if (durationSeconds <= 15) {
        reward = 5 + (durationSeconds - 5) * 1.25;
    } else {
        reward = 5 + 10 * 1.25 + (durationSeconds - 15) * 1.5;
    }
    return Math.ceil(reward);
};

// =====================
// ROUTES
// =====================

// POST
router.post('/', async (req, res) => {
    try {
        const { userId, plantId, tag, plannedDuration, actualDuration, status, startedAt, completedAt } = req.body;

        if (!userId || !plantId || !plannedDuration || !status || !startedAt) {
            return res.status(400).json({ message: 'Missing required fields' });
        }

        const devMode = process.env.DEV_MODE === 'true';
        const coinsEarned = status === 'completed'
            ? devMode ? calculateRewardDev(actualDuration) : calculateReward(actualDuration)
            : 0;

        console.log('[SESSION] Creating session, coinsEarned:', coinsEarned);

        const session = await Session.create({
            userId,
            plantId,
            tag: tag || null,
            plannedDuration,
            actualDuration,
            status,
            coinsEarned,
            startedAt,
            completedAt: completedAt || null
        });

        console.log('[SESSION] Saved:', session._id);

        if (status === 'completed') {
            await UserWallet.findOneAndUpdate(
                { userId },
                { $inc: { coins: coinsEarned } }
            );
        }

        const populated = await session.populate('plantId');
        res.status(201).json({ session: populated, coinsEarned });

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