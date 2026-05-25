const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const dotenv = require('dotenv');
const User = require('../models/User');
const UserTags = require('../models/UserTags');
const UserWallet = require('../models/UserWallet');
const { getRandomDefaultAvatar } = require('../utils/avatarService');

dotenv.config();

const DEV_MODE = process.env.DEV_MODE === 'true';
const COMMON_PASSWORD = 'Haslo123!';

const users = [
    // Verified users — can log in immediately
    { username: 'Koala', email: 'koala@jardinito.com', isVerified: true },
    { username: 'Panda', email: 'panda@jardinito.com', isVerified: true },
    { username: 'Limon', email: 'limon@jardinito.com', isVerified: true },

    // Unverified users — for testing email verification flow
    { username: 'Mango', email: 'mango@jardinito.com', isVerified: false },
    { username: 'Breza', email: 'breza@jardinito.com', isVerified: false },
];

const seed = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('MongoDB connected');

        const hashedPassword = await bcrypt.hash(COMMON_PASSWORD, 10);

        for (const u of users) {
            const existing = await User.findOne({
                $or: [{ email: u.email }, { username: u.username }]
            });
            if (existing) {
                console.log(`Skipping ${u.username} — already exists`);
                continue;
            }

            const user = new User({
                username: u.username,
                email: u.email,
                password: hashedPassword,
                provider: 'local',
                avatar: {
                    default: getRandomDefaultAvatar(),
                    custom: null,
                    google: null
                },
                isVerified: u.isVerified,
                // Unverified users expire after 24h (same as registration flow)
                accountExpiry: u.isVerified
                    ? null
                    : new Date(Date.now() + 24 * 60 * 60 * 1000)
            });

            await user.save();

            // Create default tags for all users
            await UserTags.create({
                userId: user._id,
                tags: [
                    { name: 'Study', color: 'twitterBlue' },
                    { name: 'Work', color: 'harvestOrange' }
                ]
            });

            // Create wallet — dev mode starts with 10000 coins for testing
            await UserWallet.create({
                userId: user._id,
                coins: DEV_MODE ? 10000 : 0
            });

            console.log(`Created: ${u.username} (${u.email}) — verified: ${u.isVerified}`);
        }

        console.log('Done!');
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
};

seed();