const express = require('express');
const router = express.Router();
const UserTags = require('../models/UserTags');

router.get('/', async (req, res) => {
    try {
        const { userId } = req.query;

        if (!userId) {
            return res.status(400).json({ message: 'userId is required' });
        }

        const userTags = await UserTags.findOne({ userId });
        res.json({ tags: userTags?.tags || [] });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

router.post('/', async (req, res) => {
    try {
        const { userId, name, color } = req.body;

        if (!userId || !name || !color) {
            return res.status(400).json({ message: 'userId, name and color are required' });
        }

        if (name.length > 30) {
            return res.status(422).json({ message: 'Tag name cannot exceed 30 characters' });
        }

        const userTags = await UserTags.findOneAndUpdate(
            { userId },
            { $push: { tags: { name, color } } },
            { new: true, upsert: true }
        );

        const newTag = userTags.tags[userTags.tags.length - 1];
        res.status(201).json({ tag: newTag });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

router.put('/reorder', async (req, res) => {
    try {
        const { userId, tagIds } = req.body;

        if (!userId || !tagIds) {
            return res.status(400).json({ message: 'userId and tagIds are required' });
        }

        const userTags = await UserTags.findOne({ userId });
        console.log("UserTags found:", userTags ? "yes" : "no");

        if (!userTags) return res.status(404).json({ message: 'Tags not found' });

        const reordered = tagIds.map(id =>
            userTags.tags.find(t => t._id.toString() === id)
        ).filter(Boolean);

        console.log("Reordered count:", reordered.length, "Original count:", userTags.tags.length);

        userTags.tags = reordered;
        await userTags.save();

        res.json({ tags: userTags.tags });

    } catch (err) {
        console.error("REORDER ERROR:", err);
        res.status(500).json({ message: 'Reorder failed' });
    }
});

router.put('/:tagId', async (req, res) => {
    try {
        const { tagId } = req.params;
        const { userId, name, color } = req.body;

        if (!userId || !name || !color) {
            return res.status(400).json({ message: 'userId, name and color are required' });
        }

        if (name.length > 30) {
            return res.status(422).json({ message: 'Tag name cannot exceed 30 characters' });
        }

        const userTags = await UserTags.findOneAndUpdate(
            { userId, "tags._id": tagId },
            { $set: { "tags.$.name": name, "tags.$.color": color } },
            { new: true }
        );

        if (!userTags) return res.status(404).json({ message: 'Tag not found' });

        const updatedTag = userTags.tags.find(t => t._id.toString() === tagId);

        // TODO: zaktualizować snapshot w sesjach gdy Session model będzie gotowy

        res.json({ tag: updatedTag });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});


router.delete('/:tagId', async (req, res) => {
    try {
        const { tagId } = req.params;
        const { userId } = req.query;

        if (!userId) {
            return res.status(400).json({ message: 'userId is required' });
        }

        const userTags = await UserTags.findOneAndUpdate(
            { userId },
            { $pull: { tags: { _id: tagId } } },
            { new: true }
        );

        if (!userTags) return res.status(404).json({ message: 'Tag not found' });

        res.json({ message: 'Tag deleted successfully' });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Delete tag failed' });
    }
});

module.exports = router;