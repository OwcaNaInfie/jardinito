const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcryptjs');
const { OAuth2Client } = require('google-auth-library');
const { getRandomDefaultAvatar } = require('../utils/avatarService');

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

    if(!username || !password || !email) {
        return res.status(400).json({ message: 'Jardin: Username, email and password are required' });
    }

    try {
        const existingUser = await User.findOne({ email });
        if(existingUser) {
            return res.status(400).json({ message: 'Jardin: User already exists' });
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
          }
        });

        await newUser.save();

        res.status(201).json({
            message: 'Jardin: User created',
            email: newUser.email,
            username: newUser.username,
            userId: newUser._id,
            avatar: newUser.avatar
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Jardin: Server error' });
    }
});

// Login
router.post('/login', async (req, res) => {
  try {
    console.log("=== LOGIN REQUEST ===");
    console.log("BODY:", req.body);

    const { identifier, password } = req.body;

    const identifierClean = identifier?.trim();

    console.log("Identifier:", identifierClean);

    const user = await User.findOne({
      $or: [
        { email: identifierClean },
        { username: identifierClean }
      ]
    });

    console.log("User found:", user);

    if (!user) {
      return res.status(401).json({ message: 'Invalid credentials' });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    console.log("Password match:", isMatch);

    if (!isMatch) {
      return res.status(401).json({ message: 'Invalid credentials' });
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

module.exports = router;
