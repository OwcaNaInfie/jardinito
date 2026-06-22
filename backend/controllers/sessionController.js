const sessionService = require('../services/sessionService');

exports.createSession = async (req, res) => {
    try {
        const { userId, plantId, plannedDuration, status, startedAt } = req.body;
        if (!userId || !plantId || !plannedDuration || !status || !startedAt)
            return res.status(400).json({ message: 'Missing required fields' });

        const result = await sessionService.createSession(req.body);
        res.status(201).json(result);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.getSessions = async (req, res) => {
    try {
        const { userId, from, to, status } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const sessions = await sessionService.getSessions(userId, { from, to, status });
        res.json({ sessions });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.getSessionsByPeriod = async (req, res) => {
    try {
        const { userId } = req.params;
        const { period } = req.query;

        const sessions = await sessionService.getSessionsByPeriod(userId, period);
        res.json({ sessions });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.updateSessionTag = async (req, res) => {
    try {
        const { tagId } = req.body;
        await sessionService.updateSessionTag(req.params.sessionId, tagId);
        res.json({ message: 'Session updated' });
    } catch (err) {
        if (err.message === 'TAG_NOT_FOUND')
            return res.status(404).json({ message: 'Tag not found' });
        res.status(500).json({ message: err.message });
    }
};