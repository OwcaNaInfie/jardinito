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
const getSessionDates = (userIndex) => {
    const now = new Date();

    const today = (offsetMinutes) => {
        const d = new Date(now);
        d.setMinutes(d.getMinutes() - offsetMinutes);
        return d;
    };

    const thisWeek = (daysAgo, hour) => {
        const d = new Date(now);
        d.setDate(d.getDate() - daysAgo);
        d.setHours(hour, 0, 0, 0);
        return d;
    };

    const thisMonth = (daysAgo, hour) => {
        const d = new Date(now);
        d.setDate(d.getDate() - daysAgo);
        d.setHours(hour, 0, 0, 0);
        return d;
    };

    const older = (monthsAgo, day, hour) => {
        const d = new Date(now);
        d.setMonth(d.getMonth() - monthsAgo);
        d.setDate(day);
        d.setHours(hour, 0, 0, 0);
        return d;
    };

    const sessionSets = [
        // Koala — dużo sesji dziś, kilka w tygodniu, kilka w miesiącu
        [
            { startedAt: today(30),   completedAt: today(0),    status: 'completed', plannedDuration: 30,  actualDuration: 30  },
            { startedAt: today(100),  completedAt: today(40),   status: 'completed', plannedDuration: 60,  actualDuration: 60  },
            { startedAt: today(175),  completedAt: today(145),  status: 'failed',    plannedDuration: 60,  actualDuration: 30  },
            { startedAt: today(250),  completedAt: today(160),  status: 'completed', plannedDuration: 90,  actualDuration: 90  },
            { startedAt: thisWeek(2, 10), completedAt: thisWeek(2, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(3, 14), completedAt: thisWeek(3, 15), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(5, 9),  completedAt: thisWeek(5, 9),  status: 'failed',    plannedDuration: 90, actualDuration: 25 },
            { startedAt: thisMonth(10, 11), completedAt: thisMonth(10, 12), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(18, 16), completedAt: thisMonth(18, 17), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(2, 5, 10), completedAt: older(2, 5, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
        ],
        // Panda — brak sesji dziś, kilka w tygodniu, więcej w miesiącu
        [
            { startedAt: thisWeek(1, 8),  completedAt: thisWeek(1, 9),  status: 'completed', plannedDuration: 60,  actualDuration: 60  },
            { startedAt: thisWeek(2, 13), completedAt: thisWeek(2, 14), status: 'failed',    plannedDuration: 90,  actualDuration: 40  },
            { startedAt: thisWeek(4, 10), completedAt: thisWeek(4, 11), status: 'completed', plannedDuration: 60,  actualDuration: 60  },
            { startedAt: thisWeek(6, 15), completedAt: thisWeek(6, 16), status: 'completed', plannedDuration: 60,  actualDuration: 60  },
            { startedAt: thisMonth(8, 9),  completedAt: thisMonth(8, 10),  status: 'completed', plannedDuration: 30, actualDuration: 30 },
            { startedAt: thisMonth(12, 11), completedAt: thisMonth(12, 12), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(15, 14), completedAt: thisMonth(15, 14), status: 'failed',    plannedDuration: 60, actualDuration: 15 },
            { startedAt: thisMonth(20, 10), completedAt: thisMonth(20, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(1, 10, 12), completedAt: older(1, 10, 13), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(3, 15, 9),  completedAt: older(3, 15, 10), status: 'completed', plannedDuration: 60, actualDuration: 60 },
        ],
        // Limon — jedna sesja dziś, dużo w tygodniu, mało w miesiącu
        [
            { startedAt: today(60),  completedAt: today(0),   status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(1, 9),  completedAt: thisWeek(1, 10),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(1, 14), completedAt: thisWeek(1, 15),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(2, 11), completedAt: thisWeek(2, 12),  status: 'failed',    plannedDuration: 90, actualDuration: 50 },
            { startedAt: thisWeek(3, 10), completedAt: thisWeek(3, 11),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(4, 16), completedAt: thisWeek(4, 17),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(5, 9),  completedAt: thisWeek(5, 10),  status: 'completed', plannedDuration: 30, actualDuration: 30 },
            { startedAt: thisMonth(12, 10), completedAt: thisMonth(12, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(22, 14), completedAt: thisMonth(22, 14), status: 'failed',    plannedDuration: 60, actualDuration: 20 },
            { startedAt: older(2, 8, 11), completedAt: older(2, 8, 12),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
        ],
        // Mango — kilka dziś, kilka w tygodniu, kilka w miesiącu (równomierny rozkład)
        [
            { startedAt: today(45),  completedAt: today(15),  status: 'completed', plannedDuration: 30, actualDuration: 30 },
            { startedAt: today(150), completedAt: today(90),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(2, 10), completedAt: thisWeek(2, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisWeek(3, 15), completedAt: thisWeek(3, 15), status: 'failed',    plannedDuration: 60, actualDuration: 10 },
            { startedAt: thisWeek(5, 11), completedAt: thisWeek(5, 12), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(9, 9),  completedAt: thisMonth(9, 10),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(14, 13), completedAt: thisMonth(14, 14), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(21, 10), completedAt: thisMonth(21, 10), status: 'failed',    plannedDuration: 90, actualDuration: 35 },
            { startedAt: older(1, 12, 14), completedAt: older(1, 12, 15), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(4, 20, 10), completedAt: older(4, 20, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
        ],
        // Breza — tylko sesje starsze niż tydzień i miesiąc (do testowania pustych stanów)
        [
            { startedAt: thisMonth(8, 10),  completedAt: thisMonth(8, 11),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: thisMonth(10, 14), completedAt: thisMonth(10, 15), status: 'failed',    plannedDuration: 60, actualDuration: 25 },
            { startedAt: thisMonth(15, 9),  completedAt: thisMonth(15, 10), status: 'completed', plannedDuration: 30, actualDuration: 30 },
            { startedAt: thisMonth(20, 11), completedAt: thisMonth(20, 12), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(2, 5, 10),  completedAt: older(2, 5, 11),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(2, 12, 14), completedAt: older(2, 12, 15), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(3, 8, 9),   completedAt: older(3, 8, 10),  status: 'failed',    plannedDuration: 90, actualDuration: 45 },
            { startedAt: older(3, 20, 11), completedAt: older(3, 20, 12), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(5, 15, 10), completedAt: older(5, 15, 11), status: 'completed', plannedDuration: 60, actualDuration: 60 },
            { startedAt: older(6, 3, 14),  completedAt: older(6, 3, 15),  status: 'completed', plannedDuration: 60, actualDuration: 60 },
        ],
    ];

    return sessionSets[userIndex % sessionSets.length];
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
            const sessionDates = getSessionDates(i);
            const createdTags = userTagsDoc.tags;

            for (let s = 0; s < sessionDates.length; s++) {
                const sd = sessionDates[s];
                const plant = unlockedPlants[Math.floor(Math.random() * unlockedPlants.length)];
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

        // ============================================================
        // Dedykowany użytkownik dla automatycznego zestawu testów API
        //
        // Logika monet (na podstawie seedPlants.js):
        //   Darmowe:  tulip (0), poppy (0), sunflower (0)  -- odblokowane
        //   Płatne:   daffodil (200), rose (260), bloodroot (320),
        //             gerbera (440), pansy (500), lavender (560),
        //             lily (680), orchid (780)              -- NIE odblokowane
        //
        //   250 monet startowych + 25 z T11 = 275 monet łącznie
        //   T21: kupuje daffodil (200)  →  275 - 200 = 75 monet
        //   T22: próbuje kupić orchid (780)  →  75 < 780  →  FAIL ✓
        // ============================================================
        const apiTestEmail = 'apitest@jardinito.com';
        const apiTestExisting = await User.findOne({ email: apiTestEmail });

        if (apiTestExisting) {
            console.log('Skipping ApiTest — already exists');
        } else {
            const apiTestUser = new User({
                username: 'ApiTest',
                email: apiTestEmail,
                password: await bcrypt.hash('TestHaslo123!', 10),
                provider: 'local',
                avatar: {
                    default: getRandomDefaultAvatar(),
                    custom: null,
                    google: null,
                },
                isVerified: true,
            });

            await apiTestUser.save();

            // Tylko dwa domyślne tagi -- czysty stan do testowania T15-T19
            await UserTags.create({
                userId: apiTestUser._id,
                tags: [
                    { name: 'Study', color: 'twitterBlue' },
                    { name: 'Work',  color: 'harvestOrange' },
                ],
            });

            // 250 monet, odblokowane TYLKO darmowe rośliny (bez żadnej płatnej)
            // -- dzięki temu T21 zawsze ma co kupić przy pierwszym uruchomieniu
            await UserWallet.create({
                userId: apiTestUser._id,
                coins: 250,
                unlockedPlantIds: freePlants.map(p => p._id),
            });

            // Brak sesji -- czysty stan; T11/T12 dodadzą pierwsze sesje
            console.log(
                'Created: ApiTest — 250 monet, ' +
                `odblokowane: ${freePlants.length} darmowe rośliny (${freePlants.map(p => p.name).join(', ')}), ` +
                'brak sesji'
            );
        }

        console.log('Done!');
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
};

seed();