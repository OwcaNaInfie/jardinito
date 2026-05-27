const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('../models/User');
const UserTags = require('../models/UserTags');
const UserWallet = require('../models/UserWallet');
const Session = require('../models/Session');
const VerificationToken = require('../models/VerificationToken');

dotenv.config();

const clearUsers = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('MongoDB connected');

        const users = await User.find().select('_id');
        const userIds = users.map(u => u._id);

        console.log(`Found ${userIds.length} users to delete`);

        const [tags, wallets, sessions, tokens, deletedUsers] = await Promise.all([
            UserTags.deleteMany({ userId: { $in: userIds } }),
            UserWallet.deleteMany({ userId: { $in: userIds } }),
            Session.deleteMany({ userId: { $in: userIds } }),
            VerificationToken.deleteMany({ userId: { $in: userIds } }),
            User.deleteMany({ _id: { $in: userIds } })
        ]);

        console.log(`Deleted:
  - Users: ${deletedUsers.deletedCount}
  - Tags: ${tags.deletedCount}
  - Wallets: ${wallets.deletedCount}
  - Sessions: ${sessions.deletedCount}
  - Verification tokens: ${tokens.deletedCount}`);

        console.log('Done!');
        process.exit(0);
    } catch (err) {
        console.error(err);
        process.exit(1);
    }
};

clearUsers();