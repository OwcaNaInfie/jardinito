const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const { OAuth2Client } = require('google-auth-library');
const { getRandomDefaultAvatar } = require('../utils/avatarService');
const { sendVerificationEmail } = require('../utils/emailService');
const VerificationToken = require('../models/VerificationToken');

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// Register form Validation

// Check if username is available
router.get('/check-username', async (req, res) => {
  try {
    const { username } = req.query;
    console.log("CHECK USERNAME RAW:", username);

    if (!username) {
      return res.status(400).json({ message: 'Username query param is required' });
    }

    const user = await User.findOne({ username });
    console.log("USER FOUND:", user ? user.username : null);
    const usernameAvailable = !user;

    res.status(200).json({ usernameAvailable });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Check if email is available
router.get('/check-email', async (req, res) => {
  try {
    const { email } = req.query;

    if (!email) {
      return res.status(400).json({ message: 'Email query param is required' });
    }

    const user = await User.findOne({ email });
    const emailAvailable = !user;

    res.status(200).json({ emailAvailable });
  } catch (err) {
    console.error(err);
    res.status(500).json({ message: 'Server error' });
  }
});

// Registration
router.post('/register', async (req, res) => {
    console.log("REGISTER BODY:", req.body);
    const { username, email, password } = req.body;

    if (!username || !password || !email) {
        return res.status(400).json({ message: 'Username, email and password are required' });
    }

    try {
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({ message: 'User already exists' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);
        const randomAvatar = getRandomDefaultAvatar();

        const newUser = new User({
            username,
            email,
            password: hashedPassword,
            provider: 'local',
            avatar: {
                default: randomAvatar,
                custom: null,
                google: null
            },
            isVerified: false
        });

        await newUser.save();

        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();

        await VerificationToken.create({
            userId: newUser._id,
            type: 'email_verification',
            code: verificationCode,
            codeExpiry: new Date(Date.now() + 2 * 60 * 1000),
            accountExpiry: new Date(Date.now() + 24 * 60 * 60 * 1000)
        });

        await sendVerificationEmail(email, verificationCode);

        res.status(201).json({
            message: 'User created',
            email: newUser.email,
            username: newUser.username,
            userId: newUser._id,
            avatar: newUser.avatar,
            isVerified: false
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// Login
router.post('/login', async (req, res) => {
    try {
        const { identifier, password } = req.body;
        const identifierClean = identifier?.trim();

        const user = await User.findOne({
            $or: [
                { email: identifierClean },
                { username: identifierClean }
            ]
        });

        if (!user) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        if (!user.isVerified) {
            return res.status(403).json({
                message: 'Email not verified',
                userId: user._id,
                email: user.email
            });
        }

        res.status(200).json({
            message: 'Login successful',
            userId: user._id,
            email: user.email,
            username: user.username,
            avatar: user.avatar
        });

    } catch (error) {
        console.error("LOGIN ERROR:", error);
        res.status(500).json({ message: 'Server error' });
    }
});


// Google Login
router.post('/google', async (req, res) => {
  try {
    const { idToken } = req.body;

    const ticket = await client.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const email = payload.email;
    const name = payload.name;

    if (!email) {
      return res.status(400).json({ message: 'Invalid Google token' });
    }

    let user = await User.findOne({ email });

    if (!user) {
      user = new User({
        username: name || email.split('@')[0],
        email,
        password: 'GOOGLE_AUTH',
        provider: 'google',
        googleId: payload.sub,
        avatar: {
            default: getRandomDefaultAvatar(),
            custom: null,
            google: payload.picture
        }
      });

      await user.save();
    }

    res.status(200).json({
      message: 'Google login successful',
      userId: user._id,
      email: user.email,
      username: user.username,
      avatar: user.avatar
    });

  } catch (err) {
    console.error(err);
    res.status(401).json({ message: 'Google authentication failed' });
  }
});

router.post('/verify-email', async (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ message: 'userId and code are required' });
        }

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        if (user.isVerified) {
            return res.status(400).json({ message: 'Account already verified' });
        }

        const token = await VerificationToken.findOne({
            userId,
            type: 'email_verification'
        });

        if (!token) {
            return res.status(404).json({ message: 'Verification token not found' });
        }

        if (new Date() > token.codeExpiry) {
            return res.status(410).json({ message: 'Code expired' });
        }

        if (token.code !== code) {
            return res.status(400).json({ message: 'Invalid code' });
        }

        user.isVerified = true;
        await user.save();

        await VerificationToken.deleteOne({ _id: token._id });

        res.status(200).json({
            message: 'Email verified successfully',
            userId: user._id,
            email: user.email,
            username: user.username,
            avatar: user.avatar
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

router.post('/resend-verification', async (req, res) => {
    try {
        const { userId } = req.body;

        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        if (user.isVerified) {
            return res.status(400).json({ message: 'Account already verified' });
        }

        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();

        await VerificationToken.findOneAndUpdate(
            { userId, type: 'email_verification' },
            {
                code: verificationCode,
                codeExpiry: new Date(Date.now() + 2 * 60 * 1000)
            },
            { upsert: true }
        );

        await sendVerificationEmail(user.email, verificationCode);

        res.status(200).json({ message: 'Verification code resent' });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

router.post('/get-user-id', async (req, res) => {
    try {
        const { identifier } = req.body;
        const identifierClean = identifier?.trim();

        const user = await User.findOne({
            $or: [
                { email: identifierClean },
                { username: identifierClean }
            ]
        });

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.status(200).json({
            userId: user._id,
            email: user.email,
            isVerified: user.isVerified
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;
