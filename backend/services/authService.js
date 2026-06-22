const bcrypt = require('bcryptjs');
const { OAuth2Client } = require('google-auth-library');
const userRepository = require('../repositories/userRepository');
const verificationTokenRepository = require('../repositories/verificationTokenRepository');
const tagRepository = require('../repositories/tagRepository');
const walletRepository = require('../repositories/walletRepository');
const plantRepository = require('../repositories/plantRepository');
const { getRandomDefaultAvatar } = require('../utils/avatarService');
const { sendVerificationEmail, sendPasswordResetEmail } = require('../utils/emailService');

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

const generateCode = () => Math.floor(100000 + Math.random() * 900000).toString();

exports.checkUsername = async (username) => {
    console.log('CHECK USERNAME RAW:', username);
    const user = await userRepository.findByUsername(username);
    console.log('USER FOUND:', user ? user.username : null);
    return !user;
};

exports.checkEmail = async (email) => {
    const user = await userRepository.findByEmail(email);
    return !user;
};

exports.register = async (username, email, password) => {
    const existing = await userRepository.findByEmail(email);
    if (existing) throw new Error('USER_EXISTS');

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = await userRepository.create({
        username,
        email,
        password: hashedPassword,
        provider: 'local',
        avatar: { default: getRandomDefaultAvatar(), custom: null, google: null },
        isVerified: false
    });

    await tagRepository.createForUser(newUser._id, [
        { name: 'Study', color: '#4A90D9' },
        { name: 'Work', color: '#E67E22' }
    ]);

    const freePlants = await plantRepository.findFree();
    await walletRepository.createForUser(newUser._id, {
        unlockedPlantIds: freePlants.map(p => p._id),
        favouritePlantIds: [],
        coins: 0
    });

    const verificationCode = generateCode();
    await verificationTokenRepository.create({
        userId: newUser._id,
        type: 'email_verification',
        code: verificationCode,
        codeExpiry: new Date(Date.now() + 2 * 60 * 1000),
        accountExpiry: new Date(Date.now() + 24 * 60 * 60 * 1000)
    });

    await sendVerificationEmail(email, verificationCode);

    return {
        message: 'User created',
        email: newUser.email,
        username: newUser.username,
        userId: newUser._id,
        avatar: newUser.avatar,
        isVerified: false
    };
};

exports.login = async (identifier, password) => {
    const user = await userRepository.findByEmailOrUsername(identifier?.trim());
    if (!user) throw new Error('INVALID_CREDENTIALS');

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) throw new Error('INVALID_CREDENTIALS');

    if (!user.isVerified) {
        const err = new Error('NOT_VERIFIED');
        err.userId = user._id;
        err.email = user.email;
        throw err;
    }

    return {
        message: 'Login successful',
        userId: user._id,
        email: user.email,
        username: user.username,
        avatar: user.avatar
    };
};

exports.googleLogin = async (idToken) => {
    const ticket = await client.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID
    });

    const payload = ticket.getPayload();
    const { email, name } = payload;
    if (!email) throw new Error('INVALID_GOOGLE_TOKEN');

    let user = await userRepository.findByEmail(email);

    if (!user) {
        user = await userRepository.create({
            username: name || email.split('@')[0],
            email,
            password: 'GOOGLE_AUTH',
            provider: 'google',
            googleId: payload.sub,
            avatar: {
                default: getRandomDefaultAvatar(),
                custom: null,
                google: payload.picture
            },
            isVerified: true
        });

        await tagRepository.createForUser(user._id, [
            { name: 'Study', color: 'twitterBlue' },
            { name: 'Work', color: 'harvestOrange' }
        ]);

        const freePlants = await plantRepository.findFree();
        await walletRepository.createForUser(user._id, {
            unlockedPlantIds: freePlants.map(p => p._id),
            favouritePlantIds: [],
            coins: 0
        });
    }

    return {
        message: 'Google login successful',
        userId: user._id,
        email: user.email,
        username: user.username,
        avatar: user.avatar
    };
};

exports.verifyEmail = async (userId, code) => {
    const user = await userRepository.findById(userId);
    if (!user) throw new Error('USER_NOT_FOUND');
    if (user.isVerified) throw new Error('ALREADY_VERIFIED');

    const token = await verificationTokenRepository.findByUserIdAndType(userId, 'email_verification');
    if (!token) throw new Error('TOKEN_NOT_FOUND');
    if (new Date() > token.codeExpiry) throw new Error('CODE_EXPIRED');
    if (token.code !== code) throw new Error('INVALID_CODE');

    user.isVerified = true;
    await userRepository.save(user);
    await verificationTokenRepository.deleteById(token._id);

    return {
        message: 'Email verified successfully',
        userId: user._id,
        email: user.email,
        username: user.username,
        avatar: user.avatar
    };
};

exports.resendVerification = async (userId) => {
    const user = await userRepository.findById(userId);
    if (!user) throw new Error('USER_NOT_FOUND');
    if (user.isVerified) throw new Error('ALREADY_VERIFIED');

    const verificationCode = generateCode();
    await verificationTokenRepository.upsert(userId, 'email_verification', {
        code: verificationCode,
        codeExpiry: new Date(Date.now() + 2 * 60 * 1000)
    });

    await sendVerificationEmail(user.email, verificationCode);
};

exports.getUserId = async (identifier) => {
    const user = await userRepository.findByEmailOrUsername(identifier?.trim());
    if (!user) throw new Error('USER_NOT_FOUND');
    return { userId: user._id, email: user.email, isVerified: user.isVerified };
};

exports.forgotPassword = async (identifier) => {
    const user = await userRepository.findByEmailOrUsername(identifier?.trim());
    if (!user) return null; // zawsze 200 -- nie ujawniamy czy konto istnieje

    if (!user.isVerified) throw new Error('NOT_VERIFIED_RESET');
    if (user.provider === 'google') throw new Error('GOOGLE_ACCOUNT');

    const resetCode = generateCode();
    await verificationTokenRepository.upsert(user._id, 'password_reset', {
        code: resetCode,
        codeExpiry: new Date(Date.now() + 2 * 60 * 1000)
    });

    await sendPasswordResetEmail(user.email, resetCode);
    return { userId: user._id };
};

exports.resetPassword = async (userId, code, newPassword) => {
    const token = await verificationTokenRepository.findByUserIdAndType(userId, 'password_reset');
    if (!token) throw new Error('TOKEN_NOT_FOUND');
    if (new Date() > token.codeExpiry) throw new Error('CODE_EXPIRED');
    if (token.code !== code) throw new Error('INVALID_CODE');

    const hashedPassword = await bcrypt.hash(newPassword, 10);
    await userRepository.findByIdAndUpdate(userId, { password: hashedPassword });
    await verificationTokenRepository.deleteById(token._id);
};