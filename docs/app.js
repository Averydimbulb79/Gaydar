const descriptors = ['Cosmic','Flaming','Macho','Disco','Velvet','Neon','Glitter','Royal','Turbo','Drama','Legendary','Electric','Midnight','Diamond','Chaotic','Fabulous','Galactic','Platinum','Spicy','Golden','Sassy','Lunar','Iconic','Supersonic','Majestic','Champagne','Wild','Radiant','Dazzling','Unhinged'];
const roles = ['Top','Bottom','Flex','Side'];
const wildcards = ['Leather Legend','Uniform Enthusiast','Latex Luminary','Boot Admirer','Costume Connoisseur','Silk Superstar','Masked Mystery','Roleplay Royalty','Velvet Visionary','Harness Hero','Glove Devotee','Satin Sensation'];
let previous = 'COSMIC FLEX';
document.querySelector('#reroll').addEventListener('click', () => {
  let persona;
  do { persona = `${descriptors[Math.floor(Math.random()*descriptors.length)]} ${roles[Math.floor(Math.random()*roles.length)]}`.toUpperCase(); } while (persona === previous);
  previous = persona;
  document.querySelector('#demo-persona').textContent = persona;
  const energy = document.querySelector('#demo-energy');
  energy.replaceChildren(document.createTextNode(`${70+Math.floor(Math.random()*31)} `));
  const suffix = document.createElement('small'); suffix.textContent = '/ 100'; energy.append(suffix);
  document.querySelector('#demo-wildcard').textContent = wildcards[Math.floor(Math.random()*wildcards.length)];
});
