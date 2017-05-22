import React, { PropTypes, Component } from 'react';
import {
  View,
  StyleSheet,
  Animated,
} from 'react-native';

import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';
import decorateMapComponent, {
  SUPPORTED,
  USES_DEFAULT_IMPLEMENTATION,
} from './decorateMapComponent';

const viewConfig = {
  uiViewClassName: 'AIR<provider>MapOverlay',
  validAttributes: {
    image: true,
  },
};

const propTypes = {
  ...View.propTypes,
  // A custom image to be used as overlay.
  image: PropTypes.any.isRequired,
  // Top left and bottom right coordinates for the overlay
  bounds: PropTypes.arrayOf(PropTypes.array.isRequired).isRequired,
  visible: PropTypes.bool,
  transparency: PropTypes.number,
  zIndex: PropTypes.number
};

class MapOverlay extends Component {

  render() {
    let image;
    let bounds = this.props.bounds;
    const re = new RegExp("^(http|https)://", "i");
    console.log('Overlay', this.props)
    if (this.props.image) {
      if (!(re.test(this.props.image.uri))) {
          debugger
          image = resolveAssetSource(this.props.image) || {};
          image = image.uri;
      } else {
        
        image = this.props.image.uri;
      }

    } else {
      return null
    }

    const AIRMapOverlay = this.getAirComponent();

    return (
      <AIRMapOverlay
        bounds={bounds}
        image={image}
        style={[styles.overlay, this.props.style]}
      />
    );
  }
}

MapOverlay.propTypes = propTypes;
MapOverlay.viewConfig = viewConfig;

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
  },
});

MapOverlay.Animated = Animated.createAnimatedComponent(MapOverlay);

module.exports = decorateMapComponent(MapOverlay, {
  componentType: 'Overlay',
  providers: {
    google: {
      ios: SUPPORTED,
      android: USES_DEFAULT_IMPLEMENTATION,
    },
  },
});
