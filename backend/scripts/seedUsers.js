const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const dotenv = require('dotenv');
const User = require('../models/User');
const { getRandomDefaultAvatar } = require('../utils/avatarService');

dotenv.config();

const users = [
    { username: 'Koala', email: 'koala@jardinito.com' },
    { username: 'Panda', email: 'panda@jardinito.com' },
    { username: 'Limon', email: 'limon@jardinito.com' },
    { username: 'Mango', email: 'mango@jardinito.com' },
    { username: 'Breza', email: 'breza@jardinito.com' },
];

const COMMON_PASSWORD = 'Haslo123!';

const seed = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('MongoDB connected');

        const hashedPassword = await bcrypt.hash(COMMON_PASSWORD, 10);

        for (const u of users) {
            const existing = await User.findOne({ email: u.email });
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
                isVerified: false,
                verificationCode: null,
                verificationCodeExpiry: null,
                accountExpiry: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000)
            });

            await user.save();
            console.log(`Created: ${u.username} (${u.email})`);
        }

        console.log('Done!');
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
};

seed();