const sessionRepository = require('../repositories/sessionRepository');
const walletRepository = require('../repositories/walletRepository');
const tagRepository = require('../repositories/tagRepository');

const devMode = process.env.DEV_MODE === 'true';

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

exports.createSession = async ({ userId, plantId, tag, plannedDuration, actualDuration, status, startedAt, completedAt }) => {
    const coinsEarned = status === 'completed'
        ? devMode ? calculateRewardDev(actualDuration) : calculateReward(actualDuration)
        : 0;

    console.log('[SESSION] Creating session, coinsEarned:', coinsEarned);

    const session = await sessionRepository.create({
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
        await walletRepository.addCoins(userId, coinsEarned);
    }

    const populated = await sessionRepository.populate(session);
    return { session: populated, coinsEarned };
};

exports.getSessions = async (userId, { from, to, status }) => {
    const filter = { userId };

    if (from || to) {
        filter.startedAt = {};
        if (from) filter.startedAt.$gte = new Date(from);
        if (to) filter.startedAt.$lte = new Date(to);
    }

    if (status) filter.status = status;

    return sessionRepository.findWithFilters(filter);
};

exports.getSessionsByPeriod = async (userId, period) => {
    const now = new Date();
    const from = new Date();

    if (period === 'day') {
        from.setHours(0, 0, 0, 0);
    } else if (period === 'week') {
        const day = now.getDay();
        const diff = day === 0 ? 6 : day - 1; // dni od poniedziałku
        from.setDate(now.getDate() - diff);
        from.setHours(0, 0, 0, 0);
    } else if (period === 'month') {
        from.setDate(1);
        from.setHours(0, 0, 0, 0);
    }

    return sessionRepository.findByUserIdSince(userId, from);
};

exports.updateSessionTag = async (sessionId, tagId) => {
    let tagSnapshot = null;

    if (tagId) {
        const userTagsDoc = await tagRepository.findByTagId(tagId);
        const tag = userTagsDoc?.tags.id(tagId);
        if (!tag) throw new Error('TAG_NOT_FOUND');
        tagSnapshot = { tagId: tag._id, name: tag.name, color: tag.color };
    }

    await sessionRepository.findByIdAndUpdate(sessionId, { tag: tagSnapshot });
};