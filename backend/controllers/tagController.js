const tagService = require('../services/tagService');

exports.getTags = async (req, res) => {
    try {
        const { userId } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });
        const tags = await tagService.getTags(userId);
        res.json({ tags });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.createTag = async (req, res) => {
    try {
        const { userId, name, color } = req.body;
        if (!userId || !name || !color)
            return res.status(400).json({ message: 'userId, name and color are required' });
        if (name.length > 30)
            return res.status(422).json({ message: 'Tag name cannot exceed 30 characters' });
        const tag = await tagService.createTag(userId, name, color);
        res.status(201).json({ tag });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.reorderTags = async (req, res) => {
    try {
        const { userId, tagIds } = req.body;
        if (!userId || !tagIds)
            return res.status(400).json({ message: 'userId and tagIds are required' });
        const tags = await tagService.reorderTags(userId, tagIds);
        if (!tags) return res.status(404).json({ message: 'Tags not found' });
        res.json({ tags });
    } catch (err) {
        console.error('REORDER ERROR:', err);
        res.status(500).json({ message: 'Reorder failed' });
    }
};

exports.updateTag = async (req, res) => {
    try {
        const { tagId } = req.params;
        const { userId, name, color } = req.body;
        if (!userId || !name || !color)
            return res.status(400).json({ message: 'userId, name and color are required' });
        if (name.length > 15)
            return res.status(422).json({ message: 'Tag name cannot exceed 15 characters' });
        const tag = await tagService.updateTag(userId, tagId, name, color);
        if (!tag) return res.status(404).json({ message: 'Tag not found' });
        res.json({ tag });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

exports.deleteTag = async (req, res) => {
    try {
        const { tagId } = req.params;
        const { userId } = req.query;
        if (!userId) return res.status(400).json({ message: 'userId is required' });
        const result = await tagService.deleteTag(userId, tagId);
        if (!result) return res.status(404).json({ message: 'Tag not found' });
        res.json({ message: 'Tag deleted successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Delete tag failed' });
    }
};