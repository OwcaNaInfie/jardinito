const express = require('express');
const router = express.Router();
const User = require('../models/User');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const fs = require('fs');
const { sendVerificationEmail } = require('../utils/emailService');
const VerificationToken = require('../models/VerificationToken');
const bcrypt = require('bcryptjs');

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

router.post('/update-username', async (req, res) => {
    try {
        const { userId, username } = req.body;

        if (!userId || !username) {
            return res.status(400).json({ message: 'userId and username are required' });
        }

        const existingUser = await User.findOne({ username });
        if (existingUser && existingUser._id.toString() !== userId) {
            return res.status(409).json({ message: 'Username already taken' });
        }

        const user = await User.findByIdAndUpdate(
            userId,
            { username },
            { new: true }
        );

        if (!user) return res.status(404).json({ message: 'User not found' });

        res.json({
            message: 'Username updated successfully',
            username: user.username
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Update username failed' });
    }
});

router.post('/request-email-change', async (req, res) => {
    try {
        const { userId, newEmail } = req.body;

        if (!userId || !newEmail) {
            return res.status(400).json({ message: 'userId and newEmail are required' });
        }

        const existingUser = await User.findOne({ email: newEmail });
        if (existingUser) {
            return res.status(409).json({ message: 'Email already taken' });
        }

        const user = await User.findById(userId);
        if (!user) return res.status(404).json({ message: 'User not found' });

        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();

        await VerificationToken.findOneAndUpdate(
            { userId, type: 'email_change' },
            {
                code: verificationCode,
                codeExpiry: new Date(Date.now() + 2 * 60 * 1000),
                newEmail
            },
            { upsert: true }
        );

        await sendVerificationEmail(newEmail, verificationCode);

        res.json({ message: 'Verification code sent to new email' });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Request email change failed' });
    }
});

router.post('/confirm-email-change', async (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ message: 'userId and code are required' });
        }

        const token = await VerificationToken.findOne({
            userId,
            type: 'email_change'
        });

        if (!token) return res.status(404).json({ message: 'Token not found' });

        if (new Date() > token.codeExpiry) {
            return res.status(410).json({ message: 'Code expired' });
        }

        if (token.code !== code) {
            return res.status(400).json({ message: 'Invalid code' });
        }

        const user = await User.findByIdAndUpdate(
            userId,
            { email: token.newEmail },
            { new: true }
        );

        await VerificationToken.deleteOne({ _id: token._id });

        res.json({
            message: 'Email updated successfully',
            email: user.email
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Confirm email change failed' });
    }
});

module.exports = router;