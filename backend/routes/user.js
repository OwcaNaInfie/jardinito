const express = require('express');
const router = express.Router();
const User = require('../models/User');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const fs = require('fs');

const upload = multer({
    storage: multer.memoryStorage(),
    limits: { fileSize: 5 * 1024 * 1024 },
});

router.post('/upload-avatar', upload.single('avatar'), async (req, res) => {
    try {
        const userId = req.body.userId;

        if (!req.file) return res.status(400).json({ message: 'No file uploaded' });
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const filename = `custom_${uuidv4()}.jpg`;
        const outputPath = path.join(__dirname, '../public/avatars', filename);

        fs.writeFileSync(outputPath, req.file.buffer);

        const user = await User.findById(userId);
        if (!user) return res.status(404).json({ message: 'User not found' });

        if (user.avatar?.custom) {
            const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
            if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
        }

        user.avatar.custom = filename;
        await user.save();

        res.json({ avatar: user.avatar });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Avatar upload failed' });
    }
});

router.post('/delete-avatar', async (req, res) => {
    try {
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const user = await User.findById(userId);
        if (!user) return res.status(404).json({ message: 'User not found' });

        if (user.avatar?.custom) {
            const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
            if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
            user.avatar.custom = null;
            await user.save();
        }

        res.json({ avatar: user.avatar });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Delete avatar failed' });
    }
});

router.post('/delete-account', async (req, res) => {
    try {
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ message: 'userId is required' });

        const user = await User.findById(userId);
        if (!user) return res.status(404).json({ message: 'User not found' });

        if (user.avatar?.custom) {
            const oldPath = path.join(__dirname, '../public/avatars', user.avatar.custom);
            if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
        }

        await User.findByIdAndDelete(userId);
        console.log(`Account deleted: ${userId}`);
        res.json({ message: 'Account deleted successfully' });

    } catch (err) {
        console.error(err);
        console.log(`Account deletetion failed: ${userId}`);
        res.status(500).json({ message: 'Delete account failed' });
    }
});

module.exports = router;