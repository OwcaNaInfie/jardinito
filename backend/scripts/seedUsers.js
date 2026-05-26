const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const dotenv = require('dotenv');
const User = require('../models/User');
const UserTags = require('../models/UserTags');
const UserWallet = require('../models/UserWallet');
const Session = require('../models/Session');
const Plant = require('../models/Plant');
const { getRandomDefaultAvatar } = require('../utils/avatarService');

dotenv.config();

const DEV_MODE = process.env.DEV_MODE === 'true';
const COMMON_PASSWORD = 'Haslo123!';

const users = [
    { username: 'Koala', email: 'koala@jardinito.com', isVerified: true },
    { username: 'Panda', email: 'panda@jardinito.com', isVerified: true },
    { username: 'Limon', email: 'limon@jardinito.com', isVerified: true },
    { username: 'Mango', email: 'mango@jardinito.com', isVerified: false },
    { username: 'Breza', email: 'breza@jardinito.com', isVerified: false },
];

const tagSets = [
    [
        { name: 'Study', color: 'twitterBlue' },
        { name: 'Work', color: 'harvestOrange' },
        { name: 'Reading', color: 'leafGreen' },
        { name: 'Exercise', color: 'coralRed' },
        { name: 'Meditation', color: 'lavenderPurple' },
    ],
    [
        { name: 'Coding', color: 'twitterBlue' },
        { name: 'Design', color: 'hotPink' },
        { name: 'Music', color: 'harvestOrange' },
        { name: 'Language', color: 'leafGreen' },
        { name: 'Research', color: 'skyBlue' },
    ],
    [
        { name: 'Math', color: 'sunflowerYellow' },
        { name: 'Writing', color: 'coralRed' },
        { name: 'Art', color: 'lavenderPurple' },
        { name: 'Science', color: 'twitterBlue' },
        { name: 'History', color: 'harvestOrange' },
    ],
    [
        { name: 'Project', color: 'leafGreen' },
        { name: 'Planning', color: 'skyBlue' },
        { name: 'Review', color: 'coralRed' },
        { name: 'Learning', color: 'twitterBlue' },
        { name: 'Practice', color: 'hotPink' },
    ],
    [
        { name: 'Morning', color: 'sunflowerYellow' },
        { name: 'Evening', color: 'lavenderPurple' },
        { name: 'Deep Work', color: 'twitterBlue' },
        { name: 'Creative', color: 'hotPink' },
        { name: 'Admin', color: 'harvestOrange' },
    ],
];

// Daty sesji — rozkład dla prezentacji filtrów: dziś, kwiecień, grudzień 2024, styczeń 2024
const getSessionDates = () => {
    const now = new Date();

    const today = (offsetMinutes) => {
        const d = new Date(now);
        d.setMinutes(d.getMinutes() - offsetMinutes);
        return d;
    };

    const april = (day, hour) => new Date(now.getFullYear(), 3, day, hour, 0, 0);
    const dec2024 = (day, hour) => new Date(2024, 11, day, hour, 0, 0);
    const jan2024 = (day, hour) => new Date(2024, 0, day, hour, 0, 0);

    return [
        // 3 sesje z dzisiaj
        { startedAt: today(95),  completedAt: today(35),  status: 'completed', plannedDuration: 60, actualDuration: 60  },
        { startedAt: today(185), completedAt: today(125), status: 'completed', plannedDuration: 60, actualDuration: 60  },
        { startedAt: today(260), completedAt: today(230), status: 'failed',    plannedDuration: 60, actualDuration: 30  },

        // 3 sesje z kwietnia tego roku
        { startedAt: april(14, 10), completedAt: april(14, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
        { startedAt: april(20, 14), completedAt: april(20, 15), status: 'completed', plannedDuration: 60, actualDuration: 60 },
        { startedAt: april(25, 9),  completedAt: april(25, 9),  status: 'failed',    plannedDuration: 90, actualDuration: 20 },

        // 2 sesje z grudnia 2024
        { startedAt: dec2024(10, 11), completedAt: dec2024(10, 13), status: 'completed', plannedDuration: 90, actualDuration: 90 },
        { startedAt: dec2024(22, 16), completedAt: dec2024(22, 17), status: 'completed', plannedDuration: 60, actualDuration: 60 },

        // 2 sesje ze stycznia 2024
        { startedAt: jan2024(5, 10),  completedAt: jan2024(5, 10),  status: 'failed',    plannedDuration: 30, actualDuration: 10 },
        { startedAt: jan2024(18, 15), completedAt: jan2024(18, 16), status: 'completed', plannedDuration: 60, actualDuration: 60 },
    ];
};

const seed = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('MongoDB connected');

        const hashedPassword = await bcrypt.hash(COMMON_PASSWORD, 10);
        const allPlants = await Plant.find();

        if (allPlants.length === 0) {
            console.error('No plants found — run seedPlants.js first');
            process.exit(1);
        }

        const freePlants = allPlants.filter(p => p.price === 0);
        const paidPlants = allPlants.filter(p => p.price > 0);

        // 7 odblokowanych: 3 darmowe + 4 pierwsze płatne
        const unlockedPlants = [
            ...freePlants,
            ...paidPlants.slice(0, 4)
        ];

        for (let i = 0; i < users.length; i++) {
            const u = users[i];

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
                accountExpiry: u.isVerified
                    ? null
                    : new Date(Date.now() + 24 * 60 * 60 * 1000)
            });

            await user.save();

            // 5 tagów — każdy user ma inny zestaw
            const tags = tagSets[i % tagSets.length];
            const userTagsDoc = await UserTags.create({ userId: user._id, tags });

            // Portfel: 7 odblokowanych roślin
            await UserWallet.create({
                userId: user._id,
                coins: DEV_MODE ? 10000 : 0,
                unlockedPlantIds: unlockedPlants.map(p => p._id)
            });

            // 10 sesji z różnymi datami
            const sessionDates = getSessionDates();
            const createdTags = userTagsDoc.tags;

            for (let s = 0; s < sessionDates.length; s++) {
                const sd = sessionDates[s];
                const plant = unlockedPlants[s % unlockedPlants.length];
                const tag = s % 3 === 2 ? null : createdTags[s % createdTags.length];

                const coinsEarned = sd.status === 'completed'
                    ? Math.ceil(sd.actualDuration <= 60
                        ? sd.actualDuration
                        : sd.actualDuration <= 90
                            ? 60 + (sd.actualDuration - 60) * 1.25
                            : 60 + 30 * 1.25 + (sd.actualDuration - 90) * 1.5)
                    : 0;

                await Session.create({
                    userId: user._id,
                    plantId: plant._id,
                    tag: tag ? { tagId: tag._id, name: tag.name, color: tag.color } : null,
                    plannedDuration: sd.plannedDuration,
                    actualDuration: sd.actualDuration,
                    status: sd.status,
                    coinsEarned,
                    startedAt: sd.startedAt,
                    completedAt: sd.status === 'completed' ? sd.completedAt : null,
                });
            }

            console.log(`Created: ${u.username} — tags: ${tags.length}, unlocked: ${unlockedPlants.length}, sessions: ${sessionDates.length}`);
        }

        console.log('Done!');
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
};

seed();